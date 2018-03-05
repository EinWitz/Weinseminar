import os
import tarfile

rootPath = "/Users/kjs/Desktop/Ausschnitt/"
seat_num = 5
size = 2000

for i in range(seat_num):
    c = 0
    n = 0
    output = None
    for j in sorted(os.listdir(rootPath + "seat_" + str(i))):
        if c == size:
            n += 1
            c = 0

        if c == 0:
            if output != None:
                output.close()
            output = tarfile.open(rootPath + "seat_" + str(i) + "_" + str(n) + ".tar.gz", "w|gz")
            print("Open " + rootPath + "seat_" + str(i) + "_" + str(n) + ".tar.gz")

        output.add(os.path.join(rootPath + "seat_" + str(i), j), "seat_" + str(i) + "_" + str(n) + "/" + os.path.basename(j), False)
        print(rootPath + "seat_" + str(i) + "/" + j + " TO " + "seat_" + str(i) + "_" + str(n) + "/" + os.path.basename(j))
        c += 1

    if output != None:
        output.close()