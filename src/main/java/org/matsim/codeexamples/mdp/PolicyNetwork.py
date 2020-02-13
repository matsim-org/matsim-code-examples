import keras
from keras.models import load_model
from flask import Flask, jsonify, request
import json
import numpy as np
import tensorflow as tf

app = Flask(__name__)
MODELNAME = "policy_network_51"
HOST = "localhost"
PORT = 8085

def load():
    # load the pre-trained Keras model (here we are using a model
    # pre-trained on ImageNet and provided by Keras, but you can
    # substitute in your own networks just as easily)
    global model,graph
    model = load_model(MODELNAME)
    graph = tf.get_default_graph()



@app.route("/get_action", methods = ["POST"])
def get_action():
    global model
    print(request.data)
    form = json.loads(request.data)
    state = form["state"]
    state = list(map(float, state))
    state = np.array([state])

    action_rate = model.predict(state)[0]
    action_rate = action_rate / np.sum(action_rate)
    print(action_rate)
    action_rate = list(action_rate)
    action_rate = list(map(str,action_rate))
    print(action_rate)
    return jsonify(action_rate = action_rate)


if __name__ == "__main__":
    load()
    app.run(HOST, port=PORT, debug = False, threaded=False)