# Python

This folder contains the python implementation of machine learning models used in ContextGuard for phishing URL detection and spam text detection, and the module used for heuristic-based spam category detection rules.

## Structure

```
python/
│
├── requirements.txt/               #Required python libraries  
├── classifiers/                    #Module for ML model implementation
    ├── assets/                     #ML model weights and dependencies
    ├── TextBinaryClassifier.py     #Text (Spam/Ham) classification implementation
    ├── URLBinaryClassifier.py      #URL (Phishing/Safe) classification implementation
├── config.json                     #Configuration for models and dependencies paths
├── datasets                        #Datasets used for training/evaluating ML models and heuristics module
├── heuristics/                     #Text spam category classification implementation
├── heuristics-sample.py            #Test sample for heuristics module
├── text-sample.py                  #Test sample for text classification ML model
├── url-sample.py                   #Test sample for URL classification ML model
└── README.md
```

---

## Requirements

Install dependencies before running:

```
pip install -r requirements.txt
```

---

## Usage

### URL Model

Example usage:

```python
from classifiers.URLBinaryClassifier import Classifier

url = "https://uk-covid-19.webredirect.org/to"

classifier = Classifier()
probs = classifier.predict(url)
label = "Phishing" if (float(probs[0]) > 0.5) else "Safe"

probs = round((float(probs[0]) * 100), 2)

print("URL: ", url)
print("Prediction: ", label)
print("Probability of URL being a Phished URL: ", probs, " %")
```

### Text Model

Example usage:

```python
from classifiers.TextBinaryClassifier import Classifier

text = "hey how are you?"
classifier = Classifier()
probs = classifier.predict(text)
spam_prob = probs[1]
label = "Spam" if (float(spam_prob) > 0.5) else "Ham"

probs = round((float(spam_prob) * 100), 2)

print("Text: ", text)
print("Prediction: ", label)
print("Probability of text being a spam: ", probs, " %")
```

### Heuristics

Example usage:

```python
from heuristics.message_reasoning_labels import MessageReasoningLabels

text = "Amazon is sending you a refunding of £32.64. Please reply with your bank account number to receive your refund."
processor = MessageReasoningLabels(text)
labels = processor.add_reasoning_labels()
print(text + " has labels: " + str(labels))
```

---

## Notes

* Python version 3.13.12 or above is required to run 
* Update the model paths in the config file if required
* Validate URL/Text datatype before parsing
* Non-Latin characters are not supported

---

## License

Refer to the MsgVerify’s license for usage terms.
