import json

import psycopg2
from tqdm import tqdm

UUID_ZEROS = '00000000-0000-0000-0000-000000000000'

with open('db_config.json') as f:
    config = json.loads(f.read())

connection = psycopg2.connect(host=config['server'],
                              database=config['db'],
                              user=config['user'],
                              password=config['password'])


def get_files(resource_id):
    """Get all files records connection to `resource_id`."""
    cursor = connection.cursor()
    cursor.execute('SELECT * FROM public.files '
                   f"WHERE resource_id = uuid('{resource_id}')")
    cols = [desc[0] for desc in cursor.description]
    return [dict(zip(cols, f)) for f in cursor]


def get_ids_of_contents_without_valid_file_id(output_file):
    automatic_ids = []  # ids that can be retrieved automatically
    problem_ids = []  # ids that will need manual work to fix
    cursor = connection.cursor()
    cursor.execute("SELECT * FROM public.contents")
    cols = [desc[0] for desc in cursor.description]
    print(*cols, sep='\t', end='\t', file=output_file)
    headers_printed = False
    for i, record in tqdm(enumerate(cursor), total=cursor.rowcount):
        content = dict(zip(cols, record))
        if content['url']:  # (usually Youtube)
            assert content['resource_id'] == content['file_id'] == UUID_ZEROS, f'bad url record: {content}'  # noqa: E501
        elif (content['resource_id'] == UUID_ZEROS
              or content['file_id'] == UUID_ZEROS):
            files = get_files(content['resource_id'])
            if not headers_printed:
                print(*files[0].keys(), sep='\t', file=output_file)
                headers_printed = True
            filtered_files = [f for f in files
                              if f['file_version'] == content['file_version']]
            if len(files) == 0:
                problem_ids.append(content['id'])
                print('MISSING:', content)
            elif len(files) == 1 or len(filtered_files) == 1:
                automatic_ids.append(content['id'])
            else:
                problem_ids.append(content['id'])
                for f_record in files:
                    print(*content.values(), *f_record.values(), sep='\t',
                          file=output_file)
        else:
            pass
            # Originally only two collections have contents like this:
            # ('b8bb8a27-2527-48ae-94b9-d64456daaba9',
            #  '3c8333aa-1fc8-4494-9560-a350e767c810')
            # "International Cinema annotations" (archived)
            # Dennis Cutchins' "Cutchins 345 W2022"
            # print(f'good record: {dict(zip(cols, record))}')
    return automatic_ids, problem_ids


with open('content_file_ids.tsv', 'w') as f:
    automatic_ids, problem_ids = get_ids_of_contents_without_valid_file_id(f)
print(problem_ids)
print(len(problem_ids))
