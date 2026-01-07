import pandas as pd
from message_reasoning_labels import MessageReasoningLabels

df = pd.read_csv("../datasets/phishing_messages_datasets/original/Dataset_10191.csv")

spam = df[df["LABEL"].isin(["spam", "smishing"])]

for m in spam.TEXT:
    processor = MessageReasoningLabels(m)
    labels = processor.add_reasoning_labels()
    print(m.strip() + " has labels: " + str(labels))