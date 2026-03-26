import pandas as pd
import ast
from sklearn.preprocessing import MultiLabelBinarizer
from sklearn.metrics import classification_report
from message_reasoning_labels import MessageReasoningLabels

# 1. Load the dataset
df = pd.read_csv('../datasets/phishing_messages_datasets/annotated with reasoning labels/v2/Dataset_10191_annotated.csv')

# 2. Filter to only spam messages (combine 'spam' and 'smishing' labels)
df['LABEL'] = df['LABEL'].replace(['smishing', 'spam'], 'spam')
df = df[df['LABEL'] == 'spam'].copy()
print(f"Evaluating on {len(df)} spam messages only (excluding ham)")

# 3. Parse the y_true column (converts string "['label']" to actual Python list)
df['y_true_list'] = df['y_true'].apply(ast.literal_eval)

# 4. Generate predictions using your class
print("Processing dataset and generating predictions...")
y_pred = []
for index, row in df.iterrows():
    # Initialize your class with the message text
    reasoner = MessageReasoningLabels(row['TEXT'])
    # Get labels based on the logic in message_reasoning_labels.py
    labels = reasoner.add_reasoning_labels()
    y_pred.append(labels)

# 5. Prepare data for Multi-Label evaluation
# Filter out 'Grammatical Errors/Poor Formatting' from evaluation
y_true_filtered = [[label for label in labels if label != 'Grammatical Errors/Poor Formatting']
                   for labels in df['y_true_list']]
y_pred_filtered = [[label for label in labels if label != 'Grammatical Errors/Poor Formatting']
                   for labels in y_pred]

# This creates a binary matrix where each column represents a label
mlb = MultiLabelBinarizer()
y_true_bin = mlb.fit_transform(y_true_filtered)
y_pred_bin = mlb.transform(y_pred_filtered)

# 6. Generate and print the report
# Note: 'Grammatical Errors/Poor Formatting' is excluded from evaluation
report = classification_report(
    y_true_bin,
    y_pred_bin,
    target_names=mlb.classes_,
    zero_division=0
)

print("\n--- CLASSIFICATION REPORT ---")
print(report)