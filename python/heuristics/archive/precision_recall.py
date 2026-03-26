import json, ast
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import MultiLabelBinarizer
from sklearn.metrics import precision_recall_curve, auc

# --- Configuration ---
LABELS_CANONICAL = [
    "Grammatical Errors/Poor Formatting", "Impersonation", "Marketing",
    "Adult content", "Urgency/Intimidation", "Link Click Pressure",
    "Financial/Personal Information Request", "Too Good To Be True",
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


def get_pr_data(file_path):
    df = pd.read_csv(file_path)
    gold = df["y_true"].apply(parse_cell).apply(canonicalize)
    pred = df["y_pred"].apply(parse_cell).apply(canonicalize)

    mlb = MultiLabelBinarizer(classes=LABELS_CANONICAL)
    y_true = mlb.fit_transform(gold)
    y_pred = mlb.transform(pred)

    # Calculate Micro-Average Precision-Recall
    precision, recall, _ = precision_recall_curve(y_true.ravel(), y_pred.ravel())
    pr_auc = auc(recall, precision)

    return recall, precision, pr_auc


# --- Load Datasets ---
path1 = "../datasets/phishing_messages_datasets/annotated with reasoning labels/Dataset_10191_tuned_eval_ready.csv"
path2 = "../datasets/phishing_messages_datasets/annotated with reasoning labels/twitter_spam_tuned_eval_ready.csv"

rec1, prec1, auc1 = get_pr_data(path1)
rec2, prec2, auc2 = get_pr_data(path2)

# --- Plotting ---
plt.figure(figsize=(10, 7))

# Plot SMS with Shading
plt.plot(rec1, prec1, color='#87CEEB', lw=3, label=f'SMS (AUC = {auc1:.2f})')
plt.fill_between(rec1, prec1, color='#87CEEB', alpha=0.2)

# Plot Twitter with Shading
plt.plot(rec2, prec2, color='#F08080', lw=3, label=f'Twitter (AUC = {auc2:.2f})')
plt.fill_between(rec2, prec2, color='#F08080', alpha=0.2)

# Formatting
plt.xlabel('Recall', fontsize=12)
plt.ylabel('Precision', fontsize=12)
plt.title('Precision-Recall Curve for all 3 datasets', fontsize=14)
plt.legend(loc="upper right")
plt.grid(True, linestyle='--', alpha=0.5)
plt.xlim([0.0, 1.0])
plt.ylim([0.0, 1.05])
plt.tight_layout()
plt.savefig('precision_recall_spam.png')
plt.show()