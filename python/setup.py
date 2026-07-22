from setuptools import setup, find_packages

setup(name='reqrio', version="0.3.1", packages=find_packages(),
      package_data={'reqrio': ['*.dll', '*.so']}, include_package_data=True, entry_points={
        'pyinstaller40': ["hook-dirs=reqrio.hooks"]
    })

'''
D:\softwares\python3.9.7\python.exe -m pip install --upgrade build
D:\softwares\python3.9.7\python.exe -m pip install --upgrade twine
D:\softwares\python3.9.7\python.exe -m build
D:\softwares\python3.9.7\python.exe -m twine upload .\dist\* --verbose
'''
