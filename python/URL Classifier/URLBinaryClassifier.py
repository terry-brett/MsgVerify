import torch.nn as nn
import torch.nn.functional as F

class URLBinaryClassifier(nn.Module):
    def __init__(self, input_dim):
        super(URLBinaryClassifier, self).__init__()
        self.fc1 = nn.Linear(input_dim, 128)
        self.fc2 = nn.Linear(128, 64)
        self.fc3 = nn.Linear(64, 32)
        self.output = nn.Linear(32, 1)  # Single output neuron for binary classification

    def forward(self, x):
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        x = F.relu(self.fc3(x))
        return self.output(x)  # No sigmoid here; we'll use BCEWithLogitsLoss