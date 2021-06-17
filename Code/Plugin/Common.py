import json
import threading
import traceback
import xmlrpc.server
from typing import List, Callable


class Vector3f:
    def __init__(self, x: float, y: float, z: float):
        self.x = x
        self.y = y
        self.z = z

    def __repr__(self) -> str:
        return '(%.3f, %.3f, %.3f)' % (self.x, self.y, self.z)


class Vector3i:
    def __init__(self, x: int, y: int, z: int):
        self.x = x
        self.y = y
        self.z = z

    def __repr__(self) -> str:
        return '(%d, %d, %d)' % (self.x, self.y, self.z)


class Mesh:
    def __init__(self, vertices: List[Vector3f], faceIndices: List[Vector3i]):
        self.vertices = vertices
        self.faceIndices = faceIndices

    def __repr__(self) -> str:
        return '{vertices: [%s], faceIndices: [%s]}' % (
            ', '.join(str(v) for v in self.vertices),
            ', '.join(str(f) for f in self.faceIndices)
        )


class SimpleURI:
    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port

    def toHTTP(self) -> str:
        return 'http://%s:%s' % (self.host, self.port)


class Prompt:
    def __init__(self):
        self._commandMap = {}

    def start(self):
        print('Starting the prompt...')
        thread = threading.Thread(target=self._job)
        thread.start()

    def setCommand(self, command: str, callback: Callable[[], None]):
        self._commandMap[command] = callback

    def _job(self):
        while 1:
            command = input('Command: ').strip()

            if command in self._commandMap:
                callback = self._commandMap[command]

                try:
                    callback()
                except:
                    traceback.print_exc()
            else:
                print('Invalid command!')


class Server:
    def __init__(
            self,
            uri: SimpleURI,
            onSendMesh: Callable[[], Mesh],
            onReceiveMesh: Callable[[Mesh], None]
    ):
        self._onSendMesh = onSendMesh
        self._onReceiveMesh = onReceiveMesh
        self._instance = xmlrpc.server.SimpleXMLRPCServer((uri.host, uri.port))
        self._instance.register_function(self.getMesh)
        self._instance.register_function(self.updateMesh)

    def start(self):
        print('Starting the server...')
        thread = threading.Thread(target=self._job)
        thread.start()

    def _job(self):
        self._instance.serve_forever()

    def getMesh(self) -> str:
        try:
            print('Got a request from the app! Sending the mesh...')
            mesh = self._onSendMesh()
            meshJSON = json.dumps(mesh, cls=CustomJSONEncoder)

            return meshJSON
        except:
            traceback.print_exc()
            return ''

    def updateMesh(self, meshJSON: str) -> bool:
        try:
            mesh = json.loads(meshJSON, object_hook=customJSONHook)
            self._onReceiveMesh(mesh)

            print(
                'Received a mesh from the app! (%d vertices, %d faces)'
                % (len(mesh.vertices), len(mesh.faceIndices))
            )

            return True
        except:
            traceback.print_exc()
            return False


class CustomJSONEncoder(json.JSONEncoder):
    def default(self, object_):
        if isinstance(object_, Vector3f):
            return {
                'x': object_.x,
                'y': object_.y,
                'z': object_.z
            }
        elif isinstance(object_, Vector3i):
            return {
                'x': object_.x,
                'y': object_.y,
                'z': object_.z
            }
        elif isinstance(object_, Mesh):
            return {
                'vertices': object_.vertices,
                'faceIndices': object_.faceIndices
            }
        else:
            return super().default(object_)


def customJSONHook(dict_):
    if ('x' in dict_) and ('y' in dict_) and ('z' in dict_):
        x = dict_['x']
        y = dict_['y']
        z = dict_['z']

        if isinstance(x, int) and isinstance(y, int) and isinstance(z, int):
            return Vector3i(x, y, z)
        else:
            return Vector3f(x, y, z)
    elif ('vertices' in dict_) and ('faceIndices' in dict_):
        return Mesh(
            vertices=customJSONHook(dict_['vertices']),
            faceIndices=customJSONHook(dict_['faceIndices'])
        )
    else:
        return dict_
