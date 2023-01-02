### Jonathan Lam
### Prof. Curro
### ECE 472 Deep Learning
### 10 / 1 / 20
### Project 4 -- CIFAR-100

import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt
import pickle

train_imgs = np.zeros((0, 3072))
train_lbls = []

# train datasets
with open('./train', 'rb') as file:
    file_data = pickle.load(file, encoding='bytes')
    train_imgs = np.vstack((train_imgs, file_data[b'data']))
    train_lbls += file_data[b'fine_labels']

train_lbls = tf.keras.utils.to_categorical(np.array(train_lbls))
train_imgs = train_imgs.reshape(-1, 3, 32, 32)
train_imgs = np.moveaxis(train_imgs, 1, -1)
print(train_lbls.shape, train_imgs.shape)

# standardize data
train_imgs = (train_imgs - np.mean(train_imgs)) / np.std(train_imgs)

# test dataset
with open('./test', 'rb') as file:
    file_data = pickle.load(file, encoding='bytes')
    test_imgs = file_data[b'data'].reshape(-1, 3, 32, 32)
    test_lbls = tf.keras.utils.to_categorical(np.array(file_data[b'fine_labels']))
    test_imgs = np.moveaxis(test_imgs, 1, -1)
    test_imgs = (test_imgs - np.mean(test_imgs)) / np.std(test_imgs)

# initial number of filters for "first stage"
num_filters = 32
layers = 12

# input: 32x32x3 (3 = # color channels)
input = tf.keras.Input(shape=(32, 32, 3))

# do an initial convolution layer to increase dimensionality
x = tf.keras.layers.Conv2D(filters=num_filters,
                           kernel_size=7,
                           strides=1,
                           padding='same',
                           kernel_regularizer=tf.keras.regularizers.l2(1e-6),
                           kernel_initializer='he_normal')(input)

for i in range(layers):

    # increase number of filters twice as you go deeper in the network
    # 1x1 convolutional layer to change dimensionality
    if i > 0 and i % (layers / 3) == 0:
        num_filters *= 2
        x = tf.keras.layers.Conv2D(filters=num_filters,
                                   kernel_size=1,
                                   padding='same',
                                   kernel_regularizer=tf.keras.regularizers.l2(1e-6),
                                   kernel_initializer='he_normal')(x)

    # first batchnorm, activation, conv2d
    unit = tf.keras.layers.BatchNormalization()(x)
    unit = tf.keras.layers.ReLU()(unit)

    # in first layer of a "block," no skip connection and use 2x2 strides to
    # decrease image dimensions, see ResNet-34 diagram; for other units, add a
    # skip connection
    if i > 0 and i % (layers / 3) == 0:
        unit = tf.keras.layers.Conv2D(filters=num_filters,
                                      kernel_size=3,
                                      padding='same',
                                      strides=2,
                                      kernel_regularizer=tf.keras.regularizers.l2(1e-6),
                                      kernel_initializer='he_normal')(unit)
        x = unit
    else:
        unit = tf.keras.layers.Conv2D(filters=num_filters,
                                      kernel_size=3,
                                      padding='same',
                                      kernel_regularizer=tf.keras.regularizers.l2(1e-6),
                                      kernel_initializer='he_normal')(unit)
        x = tf.keras.layers.Add()([x, unit])

    # second batchnorm, activation, conv2d
    unit = tf.keras.layers.BatchNormalization()(x)
    unit = tf.keras.layers.ReLU()(unit)
    unit = tf.keras.layers.Conv2D(filters=num_filters,
                                  kernel_size=3,
                                  padding='same',
                                  kernel_initializer='he_normal',
                                  kernel_regularizer=tf.keras.regularizers.l2(1e-6))(unit)
    unit = tf.keras.layers.Dropout(rate=0.1)(unit)
    x = tf.keras.layers.Add()([x, unit])

# final part: batchnorm, pooling, dense layer (logits for softmax)
x = tf.keras.layers.BatchNormalization()(x)
x = tf.keras.layers.AveragePooling2D(pool_size=8)(x)
x = tf.keras.layers.Flatten()(x)
x = tf.keras.layers.Dense(units=100,
                          kernel_initializer='he_normal',
                          kernel_regularizer=tf.keras.regularizers.L1L2(l2=1e-6))(x)

model = tf.keras.models.Model(inputs=[input], outputs=x)

# set up model loss and optimizer
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
    loss=tf.keras.losses.CategoricalCrossentropy(from_logits=True),
    metrics=[tf.keras.metrics.TopKCategoricalAccuracy(k=5)])

datagen = tf.keras.preprocessing.image.ImageDataGenerator(
    featurewise_center=False,
    samplewise_center=False,
    featurewise_std_normalization=False,
    samplewise_std_normalization=False,
    rotation_range=30,
    width_shift_range=0.1,
    height_shift_range=0.1,
    fill_mode='nearest',
    horizontal_flip=True,
    vertical_flip=True,
)
datagen.fit(train_imgs)

def learning_rate_scheduler(epoch):
    return 1e-3 * 0.99**epoch

model.fit_generator(datagen.flow(train_imgs, train_lbls),
                    callbacks=[tf.keras.callbacks.LearningRateScheduler(learning_rate_scheduler)],
                    epochs=100, verbose=1)

print('Evaluating on test dataset')
model.evaluate(test_imgs, test_lbls)
