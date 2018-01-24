import os
import shutil

def do_build(params):
    project = params['project']
    process_data = params['process_data']

    home_dir = process_data.project_home_dir
    current_dir = os.path.dirname(os.path.abspath(__file__))
    vendor_dir = process_data.dependency_vendor_dir
    temp_dir = process_data.dependency_temp_dir
        
    try:
        shutil.rmtree(path=vendor_dir)
    except:
        pass

    shutil.copytree(os.path.join(current_dir, 'build'), vendor_dir)