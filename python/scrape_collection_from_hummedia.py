"""Scrape mp4's and json annotations from hummedia."""

from datetime import timedelta
# import http.client as http_client
from io import StringIO
import json
# import logging
import os
from pprint import pprint
import re
from shutil import rmtree
import subprocess
import sys
from time import sleep
import warnings

import langcodes
import parsrt  # for parsing srt files
import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
import webvtt


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


def get_yvideo_sess_id(dev=True):
    global yvideo_url
    if dev:
        yvideo_url = 'https://yvideodev.byu.edu'
        from get_sess_token import dev as get_sess_token
    else:
        yvideo_url = 'https://yvideo.byu.edu'
        from get_sess_token import prod as get_sess_token
    from get_sess_token import yvideo_user

    r = requests.get(f'{yvideo_url}/api/get-session-id/{yvideo_user}/{get_sess_token}')
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    return json.loads(r.text)['session-id']


def get_yvideo_user_id():
    r = requests.get(f'{yvideo_url}/api/user', headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    return json.loads(r.text)['id']


def get_cookies(driver):
    """Convert selenium cookies to requests cookies."""
    cookies = {}
    selenium_cookies = driver.get_cookies()
    for cookie in selenium_cookies:
        cookies[cookie['name']] = cookie['value']
    return cookies


def create_resource(title, netid, filename):
    """Create new resource."""
    print('create_resource:', title, netid, filename, file=sys.stderr)
    complete_fname = filename + '.DONE.tmp'
    if not os.path.exists(complete_fname):
        print('Waiting for {filename} to finish downloading...', file=sys.stderr)
        while not os.path.exists(complete_fname):
            sleep(0.5)
    payload = {
               'copyrighted': True,
               'resource-name': title,
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
    r = requests.post(f'{yvideo_url}/api/resource', json=payload, headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    resource_id = json.loads(r.text)['id']
    r = requests.post(f'{yvideo_url}/api/resource/{resource_id}/add-access',
                      json={'username': netid},
                      headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    # upload file
    r = requests.post(f'{yvideo_url}/api/file',
                      files={'file': (filename.split('/')[-1],
                                      open(filename, 'rb'),
                                      'video/mp4')},
                      data={'resource-id': resource_id,
                            'file-version': FILE_VERSION,
                            'metadata': title},
                      headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    file_id = json.loads(r.text)['id']
    return resource_id, file_id


def create_collection(name):
    payload = {'archived': False,
               'collection-name': name,
               'copyrighted': True,
               'owner': yvideo_user_id,
               'public': False,
               'published': False}
    r = requests.post(f'{yvideo_url}/api/collection', json=payload, headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    collection_id = json.loads(r.text)['id']
    return collection_id


def create_content(collection_id, resource_id, vid_title, vid_description, annotations):
    # TODO what about clips? They don't appear to be stored in the vid_json....
    payload = {'allow-captions': True,
               'allow-definitions': True,
               'allow-notes': True,
               'annotations': annotations,
               'clips': '',
               'collection-id': collection_id,
               'content-type': 'video',
               'description': vid_description,
               'file-id': '00000000-0000-0000-0000-000000000000',
               'file-version': FILE_VERSION,
               'published': True,
               'resource-id': resource_id,
               'tags': '',
               'thumbnail': 'empty',
               'title': vid_title,
               'url': '',
               'views': 0,
               'words': ''}
    r = requests.post(f'{yvideo_url}/api/content', json=payload, headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    content_id = json.loads(r.text)['id']
    return content_id


def hms2seconds(input_str):
    """Convert HH:MM:SS.SSS to seconds."""
    parts = re.search(r'^(\d+):(\d+):(\d+\.?\d*)$', input_str).groups()
    parts = dict(zip(['hours', 'minutes', 'seconds'], [float(p) for p in parts]))
    return timedelta(**parts).total_seconds()


def add_subtitles(content_id, subtitles, language=FILE_VERSION, name=''):
    if not isinstance(subtitles, str):
        subtitles = json.dumps(subtitles)
    if len(language) == 2 and language.islower():
        language = langcodes.Language.make(language=language).display_name()
    payload = {'content': subtitles,
               'content-id': content_id,
               'language': language,
               'title': name,
               'words': ''}
    r = requests.post(f'{yvideo_url}/api/subtitle', json=payload, headers=yvideo_headers)
    assert 200 <= r.status_code < 300, (r.__dict__, r.request.__dict__)
    subtitle_id = json.loads(r.text)['id']


def transform_annotations(annotations):
    return annotations


def migrate_collection(collection_id):
    driver = webdriver.Chrome()
    driver.get('https://hummedia.byu.edu')
    driver.find_element_by_id('login-link').click()
    driver.find_element_by_id('byubutton').click()

    input('Ensure that you are logged in to hummedia, then press [enter]. ')
    driver.get(f'https://hummedia.byu.edu/api/v2/collection/{collection_id}')

    json_src = driver.find_element(By.TAG_NAME, 'pre').text
    collection_json = json.loads(json_src)
    title = collection_json['dc:title']
    desc = collection_json['dc:description']
    courses = collection_json['dc:relation']
    owner = collection_json['dc:creator']
    TAs = [n for n in collection_json['dc:rights']['write'] if n != owner]
    auditors = [n for n in collection_json['dc:rights']['read'] if n not in TAs + [owner]]

    pprint({'Title': title,
            'Owner': owner,
            'Description': desc,
            'Courses': courses,
            'TAs': TAs,
            'auditors': auditors})
    tmp_dir = f'{TMP_DIR}/{title}'
    try:
        rmtree(tmp_dir)
    except FileNotFoundError:
        pass
    os.makedirs(tmp_dir, exist_ok=True)

    collection_id = create_collection(title)

    # collection data and start downloads
    vids = []
    for vid_dict in collection_json['videos']:
        vid_id = vid_dict['pid']
        vid_title = vid_dict['ma:title']
        vid_description = vid_dict['ma:description']
        vid_fname = f'{tmp_dir}/{vid_title}'
        driver.get(f'https://hummedia.byu.edu/api/v2/video/{vid_id}')
        vid_json = json.loads(driver.find_element(By.TAG_NAME, 'pre').text)
        if len(vid_json['url']) > 1:
            warnings.warn(f'{vid_title} has more than one url: {vid_json["url"]}')
        vid_url = vid_json['url'][0]
        vid_extension = vid_url.split('.')[-1]
        vid_fname = f'{vid_fname}.{vid_extension}'
        # subprocess.run(['/usr/bin/wget', vid_url, '-O', f'{vid_fname}'])
        subprocess.Popen(f'/usr/bin/wget {vid_url} -O "{vid_fname}" && touch "{vid_fname}.DONE.tmp"', shell=True)
        new_vid_dict = {'id': vid_id,
                        'title': vid_title,
                        'description': vid_description,
                        'fname': vid_fname,
                        'json': vid_json,
                        'url': vid_url}
        vids.append(new_vid_dict)
        driver.get(f'https://hummedia.byu.edu/api/v2/annotation?client=popcorn&collection={collection_id}&dc:relation={vid_id}')

    for vid in vids:
        vid_annotations = driver.find_element(By.TAG_NAME, 'pre').text
        yvideo_annotations = transform_annotations(vid_annotations)

        # add resource
        resource_id, file_id = create_resource(vid['title'], owner, vid['fname'])
        # add content
        content_id = create_content(collection_id, resource_id, vid['title'],
                                    vid['description'], yvideo_annotations)

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
                subtitle_json = [{'start': hms2seconds(s.start),
                                  'end': hms2seconds(s.end),
                                  'text': s.text}
                                 for s in webvtt.read_buffer(StringIO(subtitle['txt']))]
            else:
                raise UserWarning(f'subtitles {subtitle["@id"]} could not be parsed')
                continue
            add_subtitles(content_id, subtitle_json, subtitle['language'], subtitle['name'])


if __name__ == '__main__':
    yvideo_sess_id = get_yvideo_sess_id(dev='--prod' not in sys.argv)
    yvideo_headers = {'session-id': yvideo_sess_id}
    yvideo_user_id = get_yvideo_user_id()
    print('Y-video user_id:', yvideo_user_id, file=sys.stderr)

    try:
        collection_id = sys.argv[1]
    except IndexError:
        collection_id = input('Please enter the collection id: ')
    migrate_collection(collection_id)
