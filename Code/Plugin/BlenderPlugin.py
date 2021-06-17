import os
import sys

import bpy
import mathutils

importPaths = [
    os.path.dirname(bpy.data.filepath),
    os.path.dirname(os.path.abspath(__file__))
]

for path in importPaths:
    if path not in sys.path:
        sys.path.append(path)

# ===================================================

from typing import List, Tuple

from Common import (
    Vector3f,
    Vector3i,
    Mesh,
    SimpleURI,
    Prompt,
    Server
)

DUMMY_MESH = Mesh(
    vertices=[
        Vector3f(0.0, 0.0, 0.0),
        Vector3f(1.0, 0.5, 0.0),
        Vector3f(1.0, 1.0, 0.5)
    ],
    faceIndices=[
        Vector3i(0, 1, 2)
    ]
)

COMMON_MATERIAL = bpy.data.materials.new('common')
COMMON_MATERIAL.diffuse_color = [1, 1, 1]
ADDED_MATERIAL = bpy.data.materials.new('add')
ADDED_MATERIAL.diffuse_color = [0, 1, 0]
REMOVED_MATERIAL = bpy.data.materials.new('removed')
REMOVED_MATERIAL.diffuse_color = [0, 0, 1]


def createBlenderMeshFromData(
        vertices: List[Tuple[float, float, float]],
        faceIndices: List[Tuple[int, int, int]],
        objectName: str,
        meshName: str
):
    blenderMesh = bpy.data.meshes.new(meshName)
    blenderObject = bpy.data.objects.new(objectName, blenderMesh)

    scene = bpy.context.scene
    scene.objects.link(blenderObject)
    scene.objects.active = blenderObject
    blenderObject.select = True

    blenderMesh.from_pydata(vertices, [], faceIndices)


def createBlenderMeshFromMesh(mesh: Mesh, objectName: str, meshName: str):
    createBlenderMeshFromData(
        [(v.x, v.y, v.z) for v in mesh.vertices],
        [(f.x, f.y, f.z) for f in mesh.faceIndices],
        objectName,
        meshName
    )


def createBlenderMeshFromOBJFile(path: str, objectName: str, meshName: str):
    with open(bpy.path.abspath('//' + path)) as p:
        lines = p.readlines()

    vertices = []
    faceIndices = []

    for n in lines:
        tokens = n.split()

        if len(tokens) == 0:
            continue

        dataType = tokens[0]

        if dataType == 'v':
            vertices.append((
                float(tokens[1]),
                float(tokens[2]),
                float(tokens[3])
            ))
        elif dataType == 'f':
            indices = [
                int(tokens[i].split('/')[0]) - 1
                for i in range(1, len(tokens))
            ]

            for i in range(1, len(indices) - 1):
                faceIndices.append((indices[0], indices[i], indices[i + 1]))

    createBlenderMeshFromData(vertices, faceIndices, objectName, meshName)


def convertBlenderMeshToMesh(objectName: str, meshName: str) -> Mesh:
    blenderObject = bpy.data.objects[objectName]
    blenderMesh = bpy.data.meshes[meshName]

    blenderObject.data.transform(blenderObject.matrix_world)
    blenderObject.matrix_world = mathutils.Matrix()

    vertices = []
    faceIndices = []

    for v in blenderMesh.vertices:
        data = v.co[:]
        vertices.append(Vector3f(data[0], data[1], data[2]))

    for f in blenderMesh.polygons:
        data = f.vertices[:]
        faceIndices.append(Vector3i(data[0], data[1], data[2]))

    return Mesh(
        vertices=vertices,
        faceIndices=faceIndices
    )


def blenderMeshExists(objectName: str):
    try:
        _ = bpy.data.objects[objectName]
        return True
    except KeyError:
        return False


def deleteBlenderMesh(objectName: str, meshName: str):
    blenderMesh = bpy.data.meshes[meshName]
    blenderObject = bpy.data.objects[objectName]
    bpy.data.meshes.remove(blenderMesh)
    bpy.data.objects.remove(blenderObject)


def askNames() -> Tuple[str, str]:
    objectName = input('Blender object name: ').strip()
    meshName = input('Blender mesh name: ').strip()

    return objectName, meshName


def onSendMesh() -> Mesh:
    objectName, meshName = askNames()
    return convertBlenderMeshToMesh(objectName, meshName)


def onReceiveMesh(mesh: Mesh):
    #objectName, meshName = askNames()
    objectName = 'MyObject'
    meshName = 'MyMesh'

    if blenderMeshExists(objectName):
        deleteBlenderMesh(objectName, meshName)

    createBlenderMeshFromMesh(mesh, objectName, meshName)


def onCreateCommand():
    path = input('Path: ')
    objectName, meshName = askNames()

    if blenderMeshExists(objectName):
        deleteBlenderMesh(objectName, meshName)

    createBlenderMeshFromOBJFile(path, objectName, meshName)

    # ==============================

    # blenderMesh = bpy.data.meshes[meshName]
    # blenderObject = bpy.data.objects[objectName]
    #
    # blenderMesh.materials.append(COMMON_MATERIAL)
    # blenderMesh.materials.append(ADDED_MATERIAL)
    # blenderMesh.materials.append(REMOVED_MATERIAL)
    #
    # maxIndex = len(blenderMesh.polygons)
    #
    # for index, f in enumerate(blenderMesh.polygons):
    #     if index < maxIndex / 3:
    #         f.material_index = 0
    #     elif index < maxIndex * 2 / 3:
    #         f.material_index = 1
    #     else:
    #         f.material_index = 2


def onDeleteCommand():
    objectName, meshName = askNames()

    if blenderMeshExists(objectName):
        deleteBlenderMesh(objectName, meshName)


def onHelpCommand():
    print('-- help: Print the available commands.')
    print('-- exit: Exit the program.')
    print('-- create: Create a new mesh from the file.')
    print('-- delete: Delete the current mesh.')


def onExitCommand():
    print('Exiting...')
    bpy.ops.wm.quit_blender()


prompt = Prompt()
prompt.setCommand('help', onHelpCommand)
prompt.setCommand('exit', onExitCommand)
prompt.setCommand('create', onCreateCommand)
prompt.setCommand('delete', onDeleteCommand)
prompt.start()

server = Server(SimpleURI('127.0.0.1', 8889), onSendMesh, onReceiveMesh)
server.start()
