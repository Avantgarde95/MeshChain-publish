import sys
import os

def readOBJ(path):
    with open(path, 'r') as p:
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

    return vertices, faceIndices

def createOBJ(vertices, faceIndices):
    lines = []

    for v in vertices:
        lines.append('v %f %f %f' % (v[0], v[1], v[2]))

    for f in faceIndices:
        lines.append('f %d %d %d' % (f[0] + 1, f[1] + 1, f[2] + 1))

    return '\n'.join(lines)

path = sys.argv[1]
v, f = readOBJ(path)
obj = createOBJ(v, f)

with open(os.path.join('copy', path), 'w', newline='\n') as p:
    p.write(obj)

print('- Vertices: %d' % len(v))
print('- Triangles: %d' % len(f))
print('- Size (.obj): %fMB' % (len(obj) / (1024.0 * 1024.0)))

