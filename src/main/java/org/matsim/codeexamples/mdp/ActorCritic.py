import os
import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from torch.distributions import Categorical
from flask import Flask, jsonify, request
import json
import numpy as np

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

lr = 0.0001

DIRECTORY = "/Users/ankitanand/matsim/matsim-code-examples/models"
PORT = 8080
HOST = "localhost"

app = Flask(__name__)


class Actor(nn.Module):
    def __init__(self, state_size, action_size):
        super(Actor, self).__init__()
        self.state_size = state_size
        self.action_size = action_size
        self.linear1 = nn.Linear(self.state_size, 128)
        self.linear2 = nn.Linear(128, 256)
        self.linear3 = nn.Linear(256, self.action_size)

    def forward(self, state):
        output = F.relu(self.linear1(state))
        output = F.relu(self.linear2(output))
        output = self.linear3(output)
        prob = F.softmax(output, dim=-1)
        distribution = Categorical(prob)
        return distribution,prob


class Critic(nn.Module):
    def __init__(self, state_size, action_size):
        super(Critic, self).__init__()
        self.state_size = state_size
        self.action_size = action_size
        self.linear1 = nn.Linear(self.state_size, 128)
        self.linear2 = nn.Linear(128, 256)
        self.linear3 = nn.Linear(256, 1)

    def forward(self, state):
        output = F.relu(self.linear1(state))
        output = F.relu(self.linear2(output))
        value = self.linear3(output)
        return value



class ActorCritic:
    def __init__(self):
        if os.path.exists(os.path.join(DIRECTORY, "actor.pkl")):
            self.actor = torch.load(os.path.join(DIRECTORY, "actor.pkl"))
        else:
            self.actor = None

        if os.path.exists(os.path.join(DIRECTORY, "critic.pkl")):
            self.critic = torch.load(os.path.join(DIRECTORY, "critic.pkl"))
        else:
            self.critic = None

        self.gamma = 0.99
        self.log_probs = []
        self.values = []
        self.masks = []
        self.entropy = 0
        self.rewards = []


    def initialize_models(self, state_size, action_size):
        self.state_size = state_size
        self.action_size = action_size
        self.actor = Actor(state_size, action_size)
        self.critic = Critic(state_size,action_size)
        if not os.path.exists(DIRECTORY):
            os.mkdir(DIRECTORY)

        torch.save(self.actor, os.path.join(DIRECTORY,"actor.pkl"))

        torch.save(self.critic, os.path.join(DIRECTORY,"critic.pkl"))

        self.free_memory()


    def get_action(self, state,outgoing_links):
        state = torch.FloatTensor(state).to(device)
        dist, prob = self.actor(state) 
        value = self.critic(state)
        while True:
            action = dist.sample()
            if int(action[0]) in outgoing_links:
                break
        log_prob = dist.log_prob(action).unsqueeze(0)
        self.log_probs.append(log_prob)
        self.entropy += dist.entropy().mean()
        self.values.append(value)
        self.masks.append(torch.tensor([1-False], dtype=torch.float, device=device))
        return action, dist

    def add_reward(self,reward):
        self.rewards.append(torch.tensor([reward], dtype=torch.float, device=device))

    def compute_returns(self,rewards, masks, gamma=0.99):
        R = 0
        returns = []
        for step in reversed(range(len(rewards))):
            R = rewards[step] + gamma * R * masks[step]
            returns.insert(0, R)
        return returns

    def free_memory(self):
        self.log_probs = []
        self.values = []
        self.masks = []
        self.entropy = 0
        self.rewards = []

    def update_models(self):
        optimizerA = optim.Adam(self.actor.parameters())
        optimizerC = optim.Adam(self.critic.parameters())

        m = min(len(self.rewards), len(self.masks), len(self.log_probs))

        l = len(self.rewards) - m
        self.rewards = self.rewards[:len(self.rewards) -l]

        l = len(self.masks) - m
        self.masks = self.masks[:len(self.masks) - l]

        l = len(self.log_probs) - m
        self.log_probs = self.log_probs[:len(self.log_probs) - l]
    


        returns = self.compute_returns(self.rewards,self.masks)

        log_probs = torch.cat(self.log_probs)
        returns = torch.cat(returns).detach()
        values = torch.cat(self.values)

        advantage = returns - values

        actor_loss = -(log_probs * advantage.detach()).mean()
        critic_loss = advantage.pow(2).mean()

        optimizerA.zero_grad()
        optimizerC.zero_grad()
        actor_loss.backward()
        critic_loss.backward()
        optimizerA.step()
        optimizerC.step()
        self.free_memory()
    
    def save_model(self):
        torch.save(self.actor,os.path.join(DIRECTORY,"actor.pkl"))
        torch.save(self.critic, os.path.join(DIRECTORY,"critic.pkl"))

actor_critic = ActorCritic()


@app.route("/initialize_models")
def initialize_models():
    print("hello")
    state_size = request.args.get("state_size")
    action_size = request.args.get("action_size")

    actor_critic.initialize_models(int(state_size), int(action_size))
    return jsonify(success = True)
    
@app.route("/get_action", methods = ["POST"])
def get_action():
    if actor_critic.actor == None or actor_critic.critic == None:
        return "Actor or Critic not initialized",400
    
    form = json.loads(request.data)
    state = form["state"]
    outgoing_links = form["outgoing_links"]
   
    outgoing_links = list(map(int,outgoing_links))
    outgoing_links = np.array(outgoing_links)
    outgoing_links = outgoing_links - 1

    if type(state) != list:
        return "State should be a list",400
    if len(state) != actor_critic.state_size:
        return "Size of state should be {}".format(self.state_size),400

    state = list(map(float,state))
    action, dist = actor_critic.get_action([state],outgoing_links)
    action = action.detach().numpy()[0]
    return jsonify(action = str(action) )

@app.route("/add_reward", methods = ["POST"])
def add_reward():
    form = json.loads(request.data);
    reward = form["reward"]
    try:
        reward = float(reward)
    except:
        return "Reward should be a double",400
    actor_critic.add_reward(reward)
    return "Success",200

@app.route("/update_models", methods = ["GET"])
def update_models():

    actor_critic.update_models()
 
    
    return "Success",200

@app.route("/save_models", methods = ["GET"])
def save_models():
    try:
        actor_critic.save_model()
    except Exception as e:
        return "Failed: {}".format(e),400
    
    return "Success",200

if __name__ == "__main__":
	app.run(HOST,PORT, debug = True)
