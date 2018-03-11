import keras.backend as K
import math
import numpy as np
import h5py
import matplotlib.pyplot as plt

from random import shuffle
# glob library kann fileNamen ziemlich effizient auslesen
import glob


def prep_data(input_path_neg, input_path_pos, dataSplit, shuffle_data = True, datatype = 'jpg'):
    '''
    macht die Vorarbeit für die create_dataset-function. Liest aus den negativ/positiv Ordnern die Bilder raus und erstellt
    labels entsprechend. Teilt dann die Daten je nach Angabe in Trainings/Testdaten ein.

    Argumente:
        input_path_neg      -- Ordner, in dem Bilder der Klasse "negativ" liegen
        input_path_neg      -- Ordner, in dem Bilder der Klasse "positiv" liegen
        dataSplit       -- Dictionary, dass die Split-Anteile für Train/Val/Test Sets enthält
        shuffle_data    -- shuffles the dataset if set on True.

    Returns:
        train_addrs     -- Pfade zu den einzelnen Bildern des trainingssets
        train_labels    -- Array, das trainings labels enthält entsprechend der Reihenfolge in train_addr
        test_addrs      -- Pfade zu den einzelnen Bildern des Testsets
        test_labels     -- Array, das test labels enthält entsprechend der Reihenfolge in test_addr
    '''

    if datatype == 'jpg':
        addrs_neg = glob.glob(input_path_neg + '/*.jpg')
        addrs_pos = glob.glob(input_path_pos + '/*.jpg')

    elif datatype == 'png':
        addrs_neg = glob.glob(input_path_neg + '/*.png')
        addrs_pos = glob.glob(input_path_pos + '/*.png')

    labels_neg = [0 for addr in addrs_neg]
    labels_pos = [1 for addr in addrs_pos]
    
    addrs = addrs_neg + addrs_pos
    labels = labels_neg + labels_pos


    if shuffle_data == True:
        try:
            c = list(zip(addrs, labels))
            shuffle(c)
            addrs, labels = zip(*c)
        except ValueError:
            print ('Es wurden keine labels ausgelesen! Höchstwahrscheinlich stimmt was nicht mit deiner Pfadangabe')
            print ('das hier soll der Pfad zu den neg Bildertn sein' + str(addrs_neg))
            print ('there are ' + str(len(addrs)) + ' adresses')
            print ('there are ' + str(len(labels)) + ' labels')

    train_split = dataSplit['train_split']
    # val_split = dataSplit['val_split']
    test_split = dataSplit['test_split']
    
    train_addrs = addrs[0:int(train_split*len(addrs))]
    train_labels = labels[0:int(train_split*len(labels))]

    test_addrs = addrs[int((1 - test_split) * len(addrs)):]
    test_labels = labels[int((1 - test_split) * len(labels)):]
    
    return train_addrs, train_labels, test_addrs, test_labels

    
def create_dataset(train_addrs, train_labels, test_addrs, test_labels, output_train_path, output_test_path, px, data_order = 'tf'):
    '''
    Erstellt aus den Daten ein h5py Dataset.

    Argumente:
        train_addrs     
        train_labels    
        test_addrs  
        test_labels
        output_train_path
        output_test_path
        px                  -- dictionary containing the width and height of the input images
        data_order          -- depends on which backend is used ('th' for Theano, 'tf' for Tensorflow)

    Returns:
        hdf5_train_file
        hdf5_test_file
    '''

    width = px['width']
    height = px['height']

    # check the order of data and chose proper data shape to save images (224 fpr the cats_dogs)
    if data_order == 'th':
        train_shape = (len(train_addrs), 3, height, width)
        #val_shape = (len(val_addrs), 3, height, width)
        test_shape = (len(test_addrs), 3, height, width)
    elif data_order == 'tf':
        train_shape = (len(train_addrs), height, width, 3)
        #val_shape = (len(val_addrs), height, width, 3)
        test_shape = (len(test_addrs), height, width, 3)


    # create an hdf5 file fo train data (will contain X-Data and Y-labels)
    hdf5_train_file = h5py.File(output_train_path, mode='w')
    # set up empty arrays to fill fill with data afterwards
    hdf5_train_file.create_dataset("train_set_x", train_shape, np.uint8, chunks=True)
    # here the shape has to be adapted according to shape of labels (softmax: 2 or sigmoid: "" used?)
    hdf5_train_file.create_dataset("train_set_y", (len(train_addrs),), np.uint8, chunks=True)
    # hdf5_train_file.create_dataset("train_set_y", (len(train_addrs),2), np.uint8)

    hdf5_test_file = h5py.File(output_test_path, mode='w')
    hdf5_test_file.create_dataset("test_set_x", test_shape, np.uint8, chunks=True)
    hdf5_test_file.create_dataset("test_set_y", (len(test_addrs),), np.uint8, chunks=True)
    # hdf5_test_file.create_dataset("test_set_y", (len(test_addrs),2), np.uint8)

    #hdf5_file.create_dataset("train_img", train_shape, np.uint8)


    #hdf5_file.create_dataset("val_img", val_shape, np.uint8)
    #hdf5_file.create_dataset("test_img", test_shape, np.uint8)


    #hdf5_file.create_dataset("train_mean", train_shape[1:], np.float32)

    #hdf5_file.create_dataset("train_labels", (len(train_addrs),), np.uint8)

    hdf5_train_file["train_set_y"][...] = train_labels

    #hdf5_file.create_dataset("val_labels", (len(val_addrs),), np.uint8)
    #hdf5_file["val_labels"][...] = val_labels

    #hdf5_file.create_dataset("test_labels", (len(test_addrs),), np.uint8)

    hdf5_test_file["test_set_y"][...] = test_labels
    
    return hdf5_train_file, hdf5_test_file
    
def load_dataset(trainset, testset):
    '''
    Ließt das zuvor erstellte h5py Dataset aus und packt die Daten in Numpy Arrays.
    Dabei muss der reshape je nach output des Netzes angepasst werden (softmax/sigmoid issue). 
    
    Argumente:


    Returns:
        train_set_x_orig
        train_set_y_orig 
        test_set_x_orig 
        test_set_y_orig
    '''


    #train_dataset = h5py.File('datasets/train_happy.h5', "r")
    # train_dataset = h5py.File('datasets/' + str(trainset), "r")
    train_set_x_orig = np.array(h5py.File('datasets/' + str(trainset), "r")["train_set_x"][:]) # your train set features
    train_set_y_orig = np.array(h5py.File('datasets/' + str(trainset), "r")["train_set_y"][:]) # your train set labels

    # test_dataset = h5py.File('datasets/' + str(testset), "r")
    test_set_x_orig = np.array(h5py.File('datasets/' + str(testset), "r")["test_set_x"][:]) # your test set features
    test_set_y_orig = np.array(h5py.File('datasets/' + str(testset), "r")["test_set_y"][:]) # your test set labels

    #classes = np.array(test_dataset["list_classes"][:]) # the list of classes

    # mit softmax/sigmoid output aufpassen und reshape anpassen!
    train_set_y_orig = train_set_y_orig.reshape((1, train_set_y_orig.shape[0]))
    test_set_y_orig = test_set_y_orig.reshape((1, test_set_y_orig.shape[0]))
    # train_set_y_orig = train_set_y_orig.reshape((2, train_set_y_orig.shape[0]))
    # test_set_y_orig = test_set_y_orig.reshape((2, test_set_y_orig.shape[0]))

    # return train_set_x_orig, train_set_y_orig, test_set_x_orig, test_set_y_orig, classes
    return train_set_x_orig, train_set_y_orig, test_set_x_orig, test_set_y_orig


def load_dataset_deprecated():
    train_dataset = h5py.File('datasets/train_happy.h5', "r")
    #train_dataset = h5py.File('datasets/dataset.hdf5', "r")
    train_set_x_orig = np.array(train_dataset["train_set_x"][:]) # your train set features
    train_set_y_orig = np.array(train_dataset["train_set_y"][:]) # your train set labels

    test_dataset = h5py.File('datasets/test_happy.h5', "r")
    test_set_x_orig = np.array(test_dataset["test_set_x"][:]) # your test set features
    test_set_y_orig = np.array(test_dataset["test_set_y"][:]) # your test set labels

    classes = np.array(test_dataset["list_classes"][:]) # the list of classes
    
    train_set_y_orig = train_set_y_orig.reshape((1, train_set_y_orig.shape[0]))
    test_set_y_orig = test_set_y_orig.reshape((1, test_set_y_orig.shape[0]))
    
    return train_set_x_orig, train_set_y_orig, test_set_x_orig, test_set_y_orig, classes

def mean_pred_deprecated(y_true, y_pred):
    '''
    deprecated, wird nicht mehr benötigt

    '''
    return K.mean(y_pred)   

def rename_deprecated(path, label, i = 1):
    '''
    benennt die Fotos im Ordner um
    Argumente: 
        path:       -- Pfad zum Order, in dem die umzubenennenden Fotos liegen
        label:      -- 'pos' oder 'neg', je nachdem, wie die Fotos umbenannt werden sollen
        i:          -- Laufvariable zur Benennung der Fotos, kann angepasst werden, falls 
                       man einem Datenset Fotos hinzufügen möchte (dann i = "höchster Wert" +1)
    '''
    files = os.listdir(path)
    if label == 'pos':
        for file in files:
            os.rename(os.path.join(path, file), os.path.join(path + 'pos.' + str(i)+'.jpg'))
            i = i+1
    if label == 'neg':    
        for file in files:
            os.rename(os.path.join(path, file), os.path.join(path + 'neg.' + str(i)+'.jpg'))
            i = i+1
            
    print ('das nächste Bild würde Nummer ' + str(i) + ' sein')


def prep_data_deprecated(input_path, dataSplit, shuffle_data = True):
    '''
    macht die Vorarbeit für die create_dataset-function. Erstellt die labels für die Daten anhand 
    deren Adresse, shuffled wenn gewünscht und erstellt je nach Einstellung Train/Val/Test Splits.

    Argumente:
        input_path      -- Ordner, in dem alle Bilder liegen, die Adresse muss eindeutig auf die Klassen-
                           zugehörigkeit schließen lassen.
        dataSplit       -- Dictionary, dass die Split-Anteile für Train/Val/Test Sets enthält
        shuffle_data    -- shuffles the dataset if set on True.

    Returns:
        train_addrs     --
        train_labels    --
        test_addrs      --
        test_labels     -- 
    '''

    # read addresses and labels from the 'train' folder
    addrs = glob.glob(input_path)
    
    # for sigmoid output (single output)
    labels = [1 if 'pos' in addr else 0 for addr in addrs]
    
    # for softmax output (outputs two numbers)
    # labels = [[1,0] if 'pos' in addr else [0,1] for addr in addrs]
    
    # shuffle data
    if shuffle_data == True:
        c = list(zip(addrs, labels))
        shuffle(c)
        addrs, labels = zip(*c)
    
    # define split ratios
    train_split = dataSplit['train_split']
    val_split = dataSplit['val_split']
    test_split = dataSplit['test_split']
    
    # Divide the data into train/validation/test set
    train_addrs = addrs[0:int(train_split*len(addrs))]
    train_labels = labels[0:int(train_split*len(labels))]

    #val_addrs = addrs[int((1 - 2 * val_split)*len(addrs)):int((1-val_split)*len(addrs))]
    #val_labels = labels[int(0.6*len(addrs)):int(0.8*len(addrs))]

    test_addrs = addrs[int((1 - test_split) * len(addrs)):]
    test_labels = labels[int((1 - test_split) * len(labels)):]  

    return train_addrs, train_labels, test_addrs, test_labels