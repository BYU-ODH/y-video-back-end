"""Scrape mp4's and json annotations from hummedia."""

import argparse
from datetime import datetime
from datetime import timedelta
from glob import glob
# import http.client as http_client
from io import StringIO
import json
# import logging
import multiprocessing
import os
from pprint import pprint
import re
from shlex import quote
# from shutil import rmtree
from shutil import which
from shutil import rmtree
import subprocess
from time import sleep
import warnings
import sys

import langcodes
import parsrt  # for parsing srt files
import requests
from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
import webvtt

import migration_config

parser = argparse.ArgumentParser(description='Scrape mp4 and json annotations from hummedia.')
parser.add_argument('-b', '--batch', default=None,
                    help='The filename of IDs of the collections on hummedia.')
parser.add_argument('-c', '--collection', default=None,
                    help='The ID of the collection on hummedia.')
parser.add_argument('-a', '--admin', default=migration_config.admin,
                    help='The admin username to user for Y-video. (Defaults to value from migration_config.py.')
parser.add_argument('-f', '--force', action='store_true',
                    help='Attempt to add everything without checking to see if it already exists. May fail from errors!')
parser.add_argument('-p', '--production', action='store_true',
                    help='Copy collection to production Y-video (without this argument, it is copied to yvideodev)')
parser.add_argument('-m', '--manual', action='store_true',
                    help='Prompt user for every possible decision.')
parser.add_argument('-u', '--user', default=None,
                    help='The netid of the new owner on Y-video (override hummedia owner).')

BATCH_IDS = None
args = parser.parse_args()
if args.batch is None and args.collection is None:
    args.collection = input('Please enter the collection id: ').strip()
elif args.batch is not None and args.collection is not None:
    raise AssertionError('Cannot use --batch and --collection arguments at the same time.')

if args.batch is not None and args.collection is None:
    with open(args.batch) as f:
        BATCH_IDS = [line.strip() for line in f if line.strip()]
elif args.batch is None and args.collection is not None:
    BATCH_IDS = [args.collection]

# These two lines enable debugging at httplib level (requests->urllib3->http.client)
# You will see the REQUEST, including HEADERS and DATA, and RESPONSE with HEADERS but without DATA.
# The only thing missing will be the response.body which is not logged.
# http_client.HTTPConnection.debuglevel = 0

# You must initialize logging, otherwise you'll not see debug output.
# logging.basicConfig()
# logging.getLogger().setLevel(logging.DEBUG)
# requests_log = logging.getLogger("requests.packages.urllib3")
# requests_log.setLevel(logging.DEBUG)
# requests_log.propagate = True


TMP_DIR = '/tmp/hummedia_migration'
FILE_VERSION = 'Cakchiquel'  # All videos are initialized with this language; users should correct them
WGET = which('wget')
WAS_WARNING = False  # If there is a warning, give message at the end to search for Warnings


def alert():
    subprocess.run(['spd-say', '-t', 'female1', 'migration needs attention'])


def add_yvideo_user(username, headers):
    payload = {'account-name': username,
               'account-role': 1,
               'account-type': 0,
               'email': f'{username}@byu.edu',
               'last-login': 'string',
               'username': username}
    r = requests.post(f'{yvideo_url}/api/user/byu/create', json=payload, headers=headers)
    assert (200 <= r.status_code < 300) or (r.status_code == 500 and 'username already taken' in r.text), (r.__dict__, r.request.__dict__)
    try:
        return json.loads(r.text)['id']
    except KeyError:
        return None  # already exists


def get_yvideo_sess_id(username=None, dev=True):
    global yvideo_url
    global yvideo_ssh
    if dev:
        yvideo_url = 'https://yvideodev.byu.edu'
        yvideo_ssh = 'yvideodev'
        get_sess_token = migration_config.dev
    else:
        yvideo_url = 'https://yvideo.byu.edu'
        yvideo_ssh = 'yvideo'
        get_sess_token = migration_config.prod

    if username is None:
        username = args.admin
    r = requests.get(f'{yvideo_url}/api/get-session-id/{username}/{get_sess_token}')
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    return json.loads(r.text)['session-id']


def get_yvideo_user_id(headers):
    r = requests.get(f'{yvideo_url}/api/user', headers=headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    return json.loads(r.text)['id']


def upload_file(vid, headers, might_skip=False):
    if might_skip:
        r = requests.get(f'{yvideo_url}/api/resource/{vid["resource_id"]}/files', headers=headers)
        files_json = json.loads(r.text)
        if files_json:
            while True:
                if args.manual:
                    alert()
                    skip_input = input('Would you like to compare the current file '
                                       'with files already uploaded to Y-video? Type '
                                       '`Y` to wait and compare. Type `n` to just '
                                       'upload the file from Hummedia. (Y/n) ')
                if not args.manual or skip_input in 'Yy':
                    yvideo_dir = '/srv/y-video-back-end/media-files'
                    for f in files_json:
                        quoted_path = quote(f'{yvideo_dir}/{f["filepath"]}')
                        file_size_cmd = quote(f'stat --format=%s {quoted_path}')
                        file_size = subprocess.check_output(f'ssh -t {yvideo_ssh} {file_size_cmd}', shell=True)
                        file_size = int(file_size.strip())
                        f['file_size'] = file_size
                        sysVsum_cmd = quote(f'sum -s {quoted_path}')
                        sysVsum = subprocess.check_output(f'ssh -t {yvideo_ssh} {sysVsum_cmd}', shell=True)
                        sysVsum, blocks, _ = sysVsum.strip().split(maxsplit=2)
                        f['sysVsum'] = int(sysVsum)
                        f['blocks'] = int(blocks)
                    break
                elif skip_input in 'Nn':
                    might_skip = False
                    break
                else:
                    print(f'Invalid input {skip_input!r}.')

    complete_fname = vid['fname'] + '.DONE.tmp'
    if not os.path.exists(complete_fname):
        print(f'Waiting for {vid["fname"]} to finish downloading...', file=log_file)
        while not os.path.exists(complete_fname):
            sleep(0.5)
    if might_skip:
        file_size = os.stat(vid['fname']).st_size
        if sys.platform == 'darwin':
            sum_cmd = f'gsum -s {quote(vid["fname"])}'
        else:
            sum_cmd = f'sum -s {quote(vid["fname"])}'
        sysVsum, blocks, _ = subprocess.check_output(sum_cmd, shell=True).strip().split(maxsplit=2)
        sysVsum, blocks = int(sysVsum), int(blocks)
        matches = [f for f in files_json
                   if f['file_size'] == file_size and f['sysVsum'] == sysVsum and f['blocks'] == blocks]
        if not matches:
            print('No matching files found. Uploading file from Hummedia...', file=log_file)
        else:
            print('The following file(s) match the file from Hummedia:', file=log_file)
            for i, m in enumerate(matches):
                print(i, ')', file=log_file)
                pprint(matches, stream=log_file)
            while True:
                if not args.manual and len(matches) == 1:
                    file_input = '0'
                else:
                    alert()
                    file_input = input('Which file should be used? (Type `X` to upload file from Hummedia) ')
                if file_input == 'X':
                    break
                elif re.match(r'[0-9]+$', file_input):
                    return matches[int(file_input)]['id']
                else:
                    print(f'Invalid input {file_input!r}', file=log_file)
    try:
        r = requests.post(f'{yvideo_url}/api/file',
                          files={'file': (vid['fname'].split('/')[-1],
                                          open(vid['fname'], 'rb'),
                                          'video/mp4')},
                          data={'resource-id': vid['resource_id'],
                                'file-version': FILE_VERSION,
                                'metadata': vid['title']},
                          headers=headers)
        assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
        file_id = json.loads(r.text)['id']
        return file_id
    except OverflowError:
        alert()
        file_id = input(f'File too large for {vid["title"]} ({vid["fname"]}). Please upload file manually now and paste file-id here: ')
        return file_id


def add_access_to_resource(resource_id, netid, headers):
    r = requests.post(f'{yvideo_url}/api/resource/{resource_id}/add-access',
                      json={'username': netid},
                      headers=headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)


def create_resource(vid, netid, headers):
    """Create new resource."""
    print('create_resource:', vid['title'], netid, vid['fname'], headers, file=log_file)
    if re.search(r'youtu\.be|youtube.com', vid['url']):
        print('\t...this is a youtube video. No resource needed.', file=log_file)
        return ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000')
    if not args.force:
        url = f'{yvideo_url}/api/admin/resource/{vid["title"].replace("/", "%252F")}'
        print(f'Getting the following url: {url}', file=log_file)
        r = requests.get(url, headers=headers)
        resources_json = json.loads(r.text)
        if resources_json:
            print(f'{len(resources_json)} resources found', file=log_file)
            for i, rsrc in enumerate(resources_json):
                print(i, ')', file=log_file)
                pprint(rsrc, stream=log_file)
            while True:
                if not args.manual and len([r for r in resources_json if r['resource-name'] == vid['title']]) == 1:
                    for i, r in enumerate(resources_json):
                        if r['resource-name'] == vid['title']:
                            rsrc_input = str(i)
                            break
                else:
                    alert()
                    rsrc_input = input(f'Which of the above resources should be used for {vid["title"]!r}? (Type X to create new resource)  ')
                if rsrc_input == 'X':
                    break
                elif re.match(r'[0-9]+$', rsrc_input):
                    vid['resource_id'] = resources_json[int(rsrc_input)]['id']
                    add_access_to_resource(vid['resource_id'], netid, headers)
                    return vid['resource_id'], upload_file(vid, headers, might_skip=True)
                else:
                    print(f'Invalid input {rsrc_input!r}. Please type an integer or "X".', file=log_file)
    payload = {
               'copyrighted': True,
               'resource-name': vid['title'],
               'physical-copy-exists': True,
               'published': True,
               'views': 0,
               'full-video': True,
               'metadata': '',
               'requester-email': netid,
               'all-file-versions': '',
               'resource-type': 'video',
               'date-validated': ''
              }
    r = requests.post(f'{yvideo_url}/api/resource', json=payload, headers=headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    vid['resource_id'] = json.loads(r.text)['id']
    add_access_to_resource(vid['resource_id'], netid, headers)
    return vid['resource_id'], upload_file(vid, headers)


def increment_name(name: str, names: list):
    names = [n for n in names if re.match(fr'{re.escape(name)}_[0-9]+', n)]
    if names:
        max_int = max(int(n.split('_')[-1]) for n in names)
        return f'{name}_{max_int + 1}'
    else:
        return f'{name}_1'


def create_collection(name, owner_id, headers):
    if not args.force:
        r = requests.get(f'{yvideo_url}/api/collections', headers=headers)
        assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
        collections = json.loads(r.text)
        matches = [c for c in collections if name and name in c['collection-name']]
        match_names = [c['collection-name'] for c in collections]
        if matches:
            pprint(list(enumerate(match_names)), stream=log_file)
            while True:
                alert()
                c_input = input(f'{name!r} already exists. Which one to use or (C)reate new or (S)kip? ')
                if re.match(r'[0-9]+$', c_input):
                    return [c for c in matches if c['collection-name'] == match_names[int(c_input)]][0]['id']
                elif c_input in 'Cc':
                    name = increment_name(name, match_names)
                    break
                elif c_input in 'Ss':
                    return
                else:
                    print(f'Invalid input {c_input!r}.', file=log_file)
    payload = {'archived': False,
               'collection-name': name,
               'copyrighted': True,
               'owner': owner_id,
               'public': False,
               'published': False}
    r = requests.post(f'{yvideo_url}/api/collection', json=payload, headers=headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    collection_id = json.loads(r.text)['id']
    return collection_id


def create_content(collection_id, vid, headers):
    print(f'create_content: collection_id={collection_id}, vid={vid}', file=log_file)
    r = requests.get(f'{yvideo_url}/api/collection/{collection_id}/contents', headers=headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    contents_json = json.loads(r.text)
    matches = [c for c in contents_json['content'] if vid['title'] and vid['title'] in c['title']]
    if not matches:
        patch = False
    else:
        pprint(list(enumerate(matches)), stream=log_file)
        print(f'Content with title {vid["title"]!r} already exists:', file=log_file)
        while True:
            if not args.manual and len(matches) == 1:
                c_input = '0'
            else:
                alert()
                c_input = input('Which one to patch or (C)reate new or (S)kip? ')
            if re.match(r'[0-9]+$', c_input):
                patch = True
                content_id = matches[int(c_input)]['id']
                break
            elif c_input in 'Cc':
                patch = False
                vid['title'] = increment_name(vid['title'], [c['title'] for c in matches])
                break
            elif c_input in 'Ss':
                return
            else:
                print(f'Invalid input {c_input!r}.', file=log_file)
    if vid['resource_id'] == '00000000-0000-0000-0000-000000000000':
        url = vid['url']
    else:
        url = ''
    payload = {'allow-captions': True,
               'allow-definitions': vid['resource_id'] != '00000000-0000-0000-0000-000000000000',
               'allow-notes': True,
               'annotations': vid['annotations'],
               'clips': '',
               'collection-id': collection_id,
               'content-type': 'video',
               'description': vid['description'] or '',
               'file-id': '00000000-0000-0000-0000-000000000000',  # TODO vid['file_id']?
               'file-version': FILE_VERSION,
               'published': True,
               'resource-id': vid['resource_id'],
               'tags': '',
               'thumbnail': 'empty',
               'title': vid['title'],
               'url': url,
               'views': 0,
               'words': ''}
    if patch:
        r = requests.patch(f'{yvideo_url}/api/content/{content_id}', json=payload, headers=headers)
        assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    else:
        r = requests.post(f'{yvideo_url}/api/content', json=payload, headers=headers)
        assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
        content_id = json.loads(r.text)['id']
    return content_id


def hms2seconds(input_str):
    """Convert HH:MM:SS.SSS to seconds."""
    parts = re.search(r'^(\d+):(\d+):(\d+\.?\d*)$', input_str).groups()
    parts = dict(zip(['hours', 'minutes', 'seconds'], [float(p) for p in parts]))
    return timedelta(**parts).total_seconds()


def add_subtitles(content_id, subtitles, headers, language=FILE_VERSION, name=''):
    if not args.force:
        r = requests.get(f'{yvideo_url}/api/content/{content_id}/subtitles', headers=headers)
        assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
        subtitles_json = json.loads(r.text)
        matches = [s for s in subtitles_json if s['content'] == subtitles]
        if matches:
            pprint(list(enumerate(matches)), stream=log_file)
            print('Subtitles matching these already exists:', file=log_file)
            while True:
                if not args.manual and len(matches) == 1:
                    s_input = '0'
                else:
                    alert()
                    s_input = input('Which one to use or (C)reate new? ')
                if re.match(r'[0-9]+$', s_input):
                    return matches[int(s_input)]['id']
                elif s_input in 'Cc':
                    name = increment_name(name, [s['title'] for s in matches])
                    break
                else:
                    print(f'Invalid input {s_input!r}.', file=log_file)
    if not isinstance(subtitles, str):
        subtitles = json.dumps(subtitles)
    if len(language) == 2 and language.islower():
        language = langcodes.Language.make(language=language).display_name()
    payload = {'content': subtitles,
               'content-id': content_id,
               'language': language,
               'title': name,
               'words': ''}
    r = requests.post(f'{yvideo_url}/api/subtitle', json=payload, headers=headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    subtitle_id = json.loads(r.text)['id']
    return subtitle_id


def transform_annotations(annotations):
    global WAS_WARNING
    print('transform_annotations:', file=log_file)
    pprint(annotations, stream=log_file)
    if not annotations:
        return '[]'
    yv_json = []

    X = {
         'skip': ('Skip', 0, '/static/media/event_skip.cbe8f9bf.svg'),
         'mutePlugin': ('Mute', 1, '/static/media/event_mute.ba25733d.svg'),
         'pause': ('Pause', 2, '/static/media/event_pause.f0719471.svg'),
         'comment': ('Comment', 3, '/static/media/event_comment.fe93f59e.svg'),
         'blank': ('Blank', 4, '/static/media/event_blank.a6b79ba2.svg'),
         }

    for track in annotations['media'][0]['tracks']:
        for e in track['trackEvents']:
            options = {}
            options['start'] = e['popcornOptions']['start']
            options['end'] = e['popcornOptions']['end']

            try:
                options['type'], options['layer'], options['icon'] = X[e['type']]
                yv_json.append(options)
            except KeyError as e:
                # WAS_WARNING = True
                # warnings.warn(f'\n{"="*79}\nWARNING\nEvent "{e["type"]}" not implemented. The following annotation was skipped: {e!r}\n{"="*79}')
                raise IOError(f'\n{"="*79}\nWARNING\nEvent "{e["type"]}" not implemented. The following annotation was skipped: {e!r}\n{"="*79}') from e
            if e['type'] == 'comment':
                options['comment'] = e['popcornOptions']['text']
                options['position'] = {'x': 50, 'y': 80}
    yv_json = sorted(yv_json, key=lambda x: float(x['start']))
    return json.dumps(yv_json)


# def download_mp4(vid_url, vid_fname):
#     subprocess.Popen(f'{WGET} -q {vid_url} -O "{vid_fname}" && touch "{vid_fname}.DONE.tmp"', shell=True)


def download_mp4(url, fname, force=False):
    global WAS_WARNING
    done_fname = f'{fname}.DONE.tmp'
    if not force and os.path.exists(fname) and os.path.exists(done_fname):
        print(f'{fname!r} already downloaded. Skipping...', file=log_file)
        return
    print(f'    downloading {fname!r} ({url})...', file=log_file)
    try:
        with requests.get(url, stream=True) as r:
            r.raise_for_status()
            with open(fname, 'wb') as f:
                for chunk in r.iter_content(chunk_size=8192):
                    f.write(chunk)
        open(done_fname, 'w').close()
    except requests.exceptions.MissingSchema:
        WAS_WARNING = True
        warnings.warn(f'Cannot download {url!r} to {fname!r}.')


def migrate_collection(args):
    global WAS_WARNING
    wd.get(f'https://hummedia.byu.edu/api/v2/collection/{args.collection}')

    json_src = wd.find_element(By.TAG_NAME, 'pre').text
    collection_json = json.loads(json_src)
    title = collection_json['dc:title']
    desc = collection_json['dc:description']
    courses = collection_json['dc:relation']
    owner = collection_json['dc:creator']
    if args.user is not None:
        owner = args.user
    owner_user_id = add_yvideo_user(owner, headers=admin_headers)
    owner_sess_id = get_yvideo_sess_id(username=owner, dev=not args.production)
    owner_headers = {'session-id': owner_sess_id}
    owner_user_id_tmp = get_yvideo_user_id(headers=owner_headers)
    if owner_user_id is not None:  # Sanity check
        assert owner_user_id == owner_user_id_tmp, (owner_user_id, owner_user_id_tmp)
    owner_user_id = owner_user_id_tmp
    print('Y-video owner user_id:', owner_user_id, file=log_file)

    TAs = [n for n in collection_json['dc:rights']['write'] if n != owner]
    auditors = [n for n in collection_json['dc:rights']['read'] if n not in TAs + [owner]]

    pprint({'Title': title,
            'Owner': owner,
            'Description': desc,
            'Courses': courses,
            'TAs': TAs,
            'auditors': auditors}, stream=log_file)
    tmp_dir = f'{TMP_DIR}/{args.collection}_{title}'
    # try:
    #     rmtree(tmp_dir)
    # except FileNotFoundError:
    #     pass
    os.makedirs(tmp_dir, exist_ok=True)

    collection_id = create_collection(title, owner_user_id, owner_headers)
    if collection_id is None:
        return
    # TODO add courses, TAs, and auditors

    # collection data
    vids = []
    for vid_dict in collection_json['videos']:
        vid_id = vid_dict['pid']
        vid_title = vid_dict['ma:title']
        vid_description = vid_dict['ma:description']
        vid_fname = f'{tmp_dir}/{vid_title.replace("/", "")}'
        annotation_ids = vid_dict['ma:hasPolicy']
        wd.get(f'https://hummedia.byu.edu/api/v2/video/{vid_id}')
        vid_json = json.loads(wd.find_element(By.TAG_NAME, 'pre').text)
        mp4s = [u for u in vid_json['url'] if u.endswith('mp4')]
        if len(vid_json['url']) == 0:
            WAS_WARNING = True
            warnings.warn(f'WARNING: No URLs available for {vid_title}.')
            vid_url = ''
        elif len(vid_json['url']) == 1:
            vid_url = vid_json['url'][0]
        elif len(vid_json['url']) > 1:
            pprint(list(enumerate(vid_json['url'])), stream=log_file)
            while True:
                if not args.manual and len(mp4s) == 1:
                    v_input = str(vid_json['url'].index(mp4s[0]))
                else:
                    alert()
                    v_input = input(f'Which URL should be used for {vid_title!r}? ')
                if re.match(r'[0-9]+$', v_input):
                    try:
                        vid_url = vid_json['url'][int(v_input)]
                        break
                    except IndexError:
                        print('Invalid input.', file=log_file)
                else:
                    print('Invalid input.', file=log_file)
        vid_extension = re.search(r'(\.(?:mp4|webm))?$', vid_url).group(1) or ''
        vid_fname = f'{vid_fname}{vid_extension}'
        new_vid_dict = {'id': vid_id,
                        'title': vid_title,
                        'description': vid_description,
                        'annotation_ids': annotation_ids,
                        'fname': vid_fname,
                        'json': vid_json,
                        'url': vid_url}
        vids.append(new_vid_dict)

    # sort vids so that shortest names are processed first
    vids = sorted(vids, key=lambda x: len(x['title']))

    downloads = [(v['url'], v['fname']) for v in vids
                 if v['url'] and not re.search(r'youtu\.be|youtube.com', v['url'])]
    with multiprocessing.Pool() as p:
        p.starmap(download_mp4, downloads)

    for vid in vids:
        if not vid['url']:
            continue
        # get annotations
        annotation_jsons = []
        for annotation_id in vid['annotation_ids']:
            wd.get(f'https://hummedia.byu.edu/api/v2/annotation/{annotation_id}?client=popcorn')
            vid_annotations = json.loads(wd.find_element(By.TAG_NAME, 'pre').text)
            if [e for t in vid_annotations['media'][0]['tracks'] for e in t['trackEvents']]:
                annotation_jsons.append(vid_annotations)
        if len(annotation_jsons) == 0:
            vid['annotations'] = '[]'
        elif len(annotation_jsons) == 1:
            vid['annotations'] = transform_annotations(annotation_jsons[0])
        elif len(annotation_jsons) > 1:
            pprint(list(enumerate(annotation_jsons)), stream=log_file)
            while True:
                alert()
                a_input = input(f'Which annotation should be used for {vid["title"]}? ')
                if re.match(r'[0-9]+$', a_input):
                    vid['annotations'] = transform_annotations(annotation_jsons[int(a_input)])
                    break
                else:
                    print('Invalid input. Must be integer from those listed.', file=log_file)

        with open(f'{vid["fname"]}.hummedia_annotation_ids.json', 'w') as f:
            print(vid['annotation_ids'], file=f)
        with open(f'{vid["fname"]}.hummedia_annotations.json', 'w') as f:
            pprint(annotation_jsons, stream=f)
        with open(f'{vid["fname"]}.yvideo_annotations.json', 'w') as f:
            print(vid['annotations'], file=f)

        # add resource
        vid['resource_id'], vid['file_id'] = create_resource(vid,
                                                             owner,
                                                             admin_headers)
        # add content
        content_id = create_content(collection_id, vid, owner_headers)
        if content_id is None:
            return

        # get subtitles
        vid_subtitles = [s for s in vid['json']['ma:hasRelatedResource']
                         if s['type'] in {'srt', 'vtt'}]
        for s in vid_subtitles:
            s['txt'] = requests.get(s['@id']).text

        # add subtitles
        for subtitle in vid_subtitles:
            if subtitle['type'] == 'srt':
                subtitle_json = [{'start': s.time[0].total_seconds(),
                                  'end': s.time[1].total_seconds(),
                                  'text': s.text}
                                 for s in parsrt.parse_str(subtitle['txt'])]
            elif subtitle['type'] == 'vtt':
                try:
                    subtitle_json = [{'start': hms2seconds(s.start),
                                      'end': hms2seconds(s.end),
                                      'text': s.text}
                                     for s in webvtt.read_buffer(StringIO(subtitle['txt']))]
                except webvtt.errors.MalformedFileError:
                    subtitle_json = []
                    pass
            else:
                WAS_WARNING = True
                warnings.warn(f'subtitles {subtitle["@id"]} could not be parsed')
                continue
            subtitle_name = subtitle['@id'].split('/')[-1].replace('.vtt', '').replace('.srt', '')
            try:
                add_subtitles(content_id, subtitle_json, owner_headers, FILE_VERSION, subtitle_name)
            except KeyError as e:
                raise IOError(str(subtitle)) from e


if __name__ == '__main__':
    TIMESTAMP = datetime.now().isoformat(timespec='seconds')
    print('Sending output to {TIMESTAMP}.log...')

    with open(f'{TIMESTAMP}.log', 'w') as log_file, open(f'{TIMESTAMP}-complete.txt', 'w') as c_file, open(f'{TIMESTAMP}-partial.txt', 'w') as p_file:
        log_file = sys.stdout  # override log_file
        print(args, file=log_file)
        print(BATCH_IDS, file=log_file)

        admin_sess_id = get_yvideo_sess_id(args.admin, dev=not args.production)
        admin_headers = {'session-id': admin_sess_id}
        admin_user_id = get_yvideo_user_id(headers=admin_headers)
        print('Y-video admin user_id:', admin_user_id, file=log_file)

        with webdriver.Chrome() as wd:
            wd.implicitly_wait(20)  # seconds
            wd.get('https://hummedia.byu.edu')
            try:
                WebDriverWait(wd, 3).until(EC.alert_is_present(),
                                           'Timed out waiting for PA creation ' +
                                           'confirmation popup to appear.')
                hummedia_alert = wd.switch_to.alert
                hummedia_alert.accept()
            except TimeoutException:
                pass
            wd.find_element(By.ID, 'login-link').click()
            wd.find_element(By.ID, 'byubutton').click()
            wd.find_element(By.ID, 'dont-trust-browser-button').click()
            input('Ensure that you are logged in to hummedia, then press [enter]. ')

            for collection in BATCH_IDS:
                args.collection = collection
                try:
                    migrate_collection(args)
                    print(args.collection, file=c_file, flush=True)
                except IOError:
                    print(args.collection, file=p_file, flush=True)
                finally:
                    cache_glob = f'/tmp/hummedia_migration/{args.collection}_*'
                    cache_dirs = glob(cache_glob)
                    if (args.batch is not None
                            or alert()  # always Falsey
                            or input(f'Delete {cache_dirs} (taken from {cache_glob})? (y/N) ') in {'Y', 'y', 'yes', 'Yes'}):
                        for cache_dir in cache_dirs:
                            rmtree(cache_dir)
                if WAS_WARNING:
                    print(f'There were warnings for {collection}. Search the output for "UserWarning".', file=log_file)
                WAS_WARNING = False
    subprocess.run(['spd-say', '-t', 'female1', 'The dishes are done!'])
