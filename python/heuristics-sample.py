from heuristics.message_reasoning_labels import MessageReasoningLabels

text = "Amazon is sending you a refunding of £32.64. Please reply with your bank account number to receive your refund."
processor = MessageReasoningLabels(text)
labels = processor.add_reasoning_labels()
print(text + " has labels: " + str(labels))