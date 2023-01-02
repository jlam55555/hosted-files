### Jonathan Lam
### Prof. Curro
### ECE 472 Deep Learning
### 9 / 20 / 20
### Project 3

### MNIST data from http://yann.lecun.com/exdb/mnist/

import numpy as np
import tensorflow as tf
import kerastuner as kt

# 32-bit big-endian byte buffer to int
def be32_to_int(buf):
    return np.ndarray(shape=(1,), dtype='>i4', buffer=buf)[0]

# see URL of MNIST data source for data layout
def read_mnist_file(filename, is_labels):
    with open(filename, 'rb') as mnist_file:
        # read header info
        be32_to_int(mnist_file.read(4))     # read and discard magic number
        num_imgs = be32_to_int(mnist_file.read(4))
        num_rows = None if is_labels else be32_to_int(mnist_file.read(4))
        num_cols = None if is_labels else be32_to_int(mnist_file.read(4))

        # read raw image data
        data = np.frombuffer(mnist_file.read(), dtype=np.uint8)
        return num_imgs, num_rows, num_cols, data

# read train/validation dataset files
num_train_file_imgs, num_rows, num_cols, train_file_imgs = \
    read_mnist_file('train-images-idx3-ubyte', False)
_, _, _, train_file_lbls = read_mnist_file('train-labels-idx1-ubyte', True)

# read test dataset files
num_test, _, _, test_imgs = read_mnist_file('t10k-images-idx3-ubyte', False)
_, _, _, test_lbls = read_mnist_file('t10k-labels-idx1-ubyte', True)

# P := # features (pixels) = num_rows x num_cols
# N := num_imgs
# reshape features to NxP, scale to [0, 1)
train_file_imgs = train_file_imgs.reshape(-1, num_rows * num_cols) / 255.
test_imgs = test_imgs.reshape(-1, num_rows * num_cols) / 255.
# reshape labels to Nx1
train_file_lbls = train_file_lbls.reshape(-1, 1)
test_lbls = test_lbls.reshape(-1, 1)

# split train/validation dataset into train and validation datasets
indices = np.arange(num_train_file_imgs)
np.random.shuffle(indices)
cutoff = 50000
train_imgs, val_imgs, train_lbls, val_lbls = \
    train_file_imgs[indices[:cutoff],:], \
    train_file_imgs[indices[cutoff:],:], \
    train_file_lbls[indices[:cutoff],:], \
    train_file_lbls[indices[cutoff:],:]

# tunable model builder
def build_model(hp):
    model = tf.keras.models.Sequential()
    model.add(tf.keras.Input(shape=(num_rows * num_cols,)))

    # tunable hidden layers
    for i in range(hp.Int('layers', 3, 5)):
        model.add(tf.keras.layers.Dense(
            units=hp.Choice('units' + str(i), [64, 128, 256]),
            kernel_regularizer=tf.keras.regularizers.L1L2(
                l2=hp.Float('l2_' + str(i), 1e-8, 1e-4, sampling='log'))))
        model.add(tf.keras.layers.BatchNormalization())
        model.add(tf.keras.layers.LeakyReLU())
        model.add(tf.keras.layers.Dropout(
            rate=hp.Float('dropout_' + str(i), 0.1, 0.5, step=0.2)))

    # final layer: calculate logits
    model.add(tf.keras.layers.Dense(
        units=10,
        kernel_regularizer=tf.keras.regularizers.L1L2(
            l2=hp.Float('l2_final', 1e-8, 1e-4, sampling='log'))))

    # set up model loss and optimizer
    model.compile(
        optimizer=tf.keras.optimizers.Adam(
            learning_rate=hp.Float('learning_rate', 1e-4, 1e-1, sampling='log')),
        loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
        metrics=['accuracy'])

    return model

# create kerastuner hyperband tuner
tuner = kt.Hyperband(build_model, objective='val_acc', max_epochs=10)

# print search space summary
tuner.search_space_summary()

# search for best hyperparameters, evaluate on validation set
tuner.search(train_imgs, train_lbls, validation_data=(val_imgs, val_lbls))

# print best result
tuner.results_summary()

# train model with best hyperparameters
best_hps = tuner.get_best_hyperparameters(num_trials=1)[0]
model = tuner.hypermodel.build(best_hps)
model.fit(train_imgs, train_lbls, epochs=10)

# evaluate model on test dataset
print('Evaluating on test dataset:')
model.evaluate(test_imgs, test_lbls, verbose=2)