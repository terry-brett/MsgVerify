import json, ast
import pandas as pd

from sklearn.preprocessing import MultiLabelBinarizer
from sklearn.metrics import classification_report, f1_score, precision_score, recall_score, jaccard_score

DATA_PATH = "../datasets/phishing_messages_datasets/annotated with reasoning labels/twitter_spam_tuned_eval_ready.csv"

LABELS_CANONICAL = [
    "Grammatical Errors/Poor Formatting",
    "Impersonation",
    "Marketing",
    "Adult content",
    "Urgency/Intimidation",
    "Link Click Pressure",
    "Financial/Personal Information Request",
    "Too Good To Be True",
    "Generic Greeting",
    "Credential Verification Request",
]

def parse_cell(x):
    if pd.isna(x) or x == "":
        return []
    if isinstance(x, list):
        return x
    s = str(x).strip()
    if not s:
        return []
    try:
        v = json.loads(s)
        return v if isinstance(v, list) else []
    except Exception:
        try:
            v = ast.literal_eval(s)
            return v if isinstance(v, list) else []
        except Exception:
            return [t.strip() for t in s.split(";") if t.strip()]

def canonicalize(labels):
    return [l.strip() for l in labels if isinstance(l, str) and l.strip()]

def evaluate(df, title=""):
    gold = df["y_true"].apply(parse_cell).apply(canonicalize)
    pred = df["y_pred"].apply(parse_cell).apply(canonicalize)

    mlb = MultiLabelBinarizer(classes=LABELS_CANONICAL)
    y_true = mlb.fit_transform(gold)
    y_pred = mlb.transform(pred)

    print("\n" + "="*80)
    print(title or "Evaluation")
    print("="*80)

    print(classification_report(y_true, y_pred, target_names=mlb.classes_, zero_division=0))

    micro_p  = precision_score(y_true, y_pred, average="micro", zero_division=0)
    micro_r  = recall_score(y_true, y_pred, average="micro", zero_division=0)
    micro_f1 = f1_score(y_true, y_pred, average="micro", zero_division=0)
    macro_f1 = f1_score(y_true, y_pred, average="macro", zero_division=0)
    jac      = jaccard_score(y_true, y_pred, average="samples", zero_division=0)

    print(f"Overall micro P/R/F1: {micro_p:.3f} / {micro_r:.3f} / {micro_f1:.3f}")
    print(f"Overall macro F1:      {macro_f1:.3f}")
    print(f"Sample Jaccard:        {jac:.3f}")


df = pd.read_csv(DATA_PATH)

evaluate(df, title="ALL messages")

if "LABEL" in df.columns:
    spam = df[df["LABEL"] == 1].copy()
    evaluate(spam, title="Spam Only")
