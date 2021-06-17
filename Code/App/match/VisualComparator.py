import sys
import json

sys.path.append('match3d')
sys.path.append('image_match')

from match3d.api_operations import APIOperations

newMeshPath = 'NewMesh.stl'
targetMeshPath = 'TargetMesh.stl'

api = APIOperations()
api.add('NewMesh', stl_file=newMeshPath)
result = api.search(stl_file=targetMeshPath)
print('Result: %s' % result)
similarity = 1.0 - list(result[targetMeshPath].values())[0]

with open('Result.txt', 'w') as p:
    p.write('%f' % similarity)

print('Similarity: %f' % similarity)
