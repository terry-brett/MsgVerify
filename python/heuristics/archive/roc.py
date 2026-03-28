import json
import ast
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import MultiLabelBinarizer
from sklearn.metrics import roc_curve, auc

# Define paths (as provided in your snippet)
PATH1 = "../datasets/phishing_messages_datasets/annotated with reasoning labels/Dataset_10191_tuned_eval_ready.csv"
PATH2 = "../datasets/phishing_messages_datasets/annotated with reasoning labels/twitter_spam_tuned_eval_ready.csv"

LABELS_CANONICAL = [
    "Grammatical Errors/Poor Formatting",
    "Impersonation",
    "Marketing",
    "Adult content",
    "Urgency/Intimidation",
    "Link Click Pressure",
    "Financial/Personal Information Request",
    "Too Good To Be True",
    "Credential Verification Request",
]


def parse_cell(x):
    if pd.isna(x) or x == "": return []
    if isinstance(x, list): return x
    s = str(x).strip()
    try:
        v = json.loads(s)
        return v if isinstance(v, list) else []
    except:
        try:
            v = ast.literal_eval(s)
            return v if isinstance(v, list) else []
        except:
            return [t.strip() for t in s.split(";") if t.strip()]


def canonicalize(labels):
    return [l.strip() for l in labels if isinstance(l, str) and l.strip()]


def get_binary_matrices(df, labels_canonical):
    gold = df["y_true"].apply(parse_cell).apply(canonicalize)
    pred = df["y_pred"].apply(parse_cell).apply(canonicalize)

    mlb = MultiLabelBinarizer(classes=labels_canonical)
    y_true = mlb.fit_transform(gold)
    y_pred = mlb.transform(pred)
    return y_true, y_pred


# Load and Filter Datasets
# Dataset 1
df1 = pd.read_csv(PATH1)
spam1 = df1[df1["LABEL"].isin(["spam", "smishing"])].copy()
y_true1, y_pred1 = get_binary_matrices(spam1, LABELS_CANONICAL)

# Dataset 2
df2 = pd.read_csv(PATH2)
spam2 = df2[df2["LABEL"] == 1].copy()
y_true2, y_pred2 = get_binary_matrices(spam2, LABELS_CANONICAL)

# Compute ROC metrics (Micro-average)
# We flatten the matrices to treat every label assignment as a binary prediction
fpr1, tpr1, _ = roc_curve(y_true1.ravel(), y_pred1.ravel())
roc_auc1 = auc(fpr1, tpr1)

fpr2, tpr2, _ = roc_curve(y_true2.ravel(), y_pred2.ravel())
roc_auc2 = auc(fpr2, tpr2)

# Plotting
plt.figure(figsize=(8, 6))
plt.plot(fpr1, tpr1, color='#87CEEB', lw=2, label=f'SMS (AUC = {roc_auc1:.2f})')
plt.plot(fpr2, tpr2, color='#F08080', lw=2, label=f'Twitter (AUC = {roc_auc2:.2f})')

plt.plot([0, 1], [0, 1], color='black', lw=1, linestyle='--')
plt.xlim([0.0, 1.0])
plt.ylim([0.0, 1.05])
plt.xlabel('False Positive Rate')
plt.ylabel('True Positive Rate')
plt.title('Micro-averaged ROC Curve for all 3 datasets')
plt.legend(loc="lower right")
plt.grid(alpha=0.3)
plt.savefig('roc_comparison_spam.png')
plt.show()