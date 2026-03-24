import TextBinaryClassifier

def main():
    text = "hey how are you?"
    classifier = TextBinaryClassifier()
    probs = classifier.predict(text)
    spam_prob = probs[1]
    label = "Spam" if (float(spam_prob) > 0.5) else "Ham"

    probs = round((float(spam_prob) * 100), 2)

    print("Text: ", text)
    print("Prediction: ", label)
    print("Probability of text being a spam: ", spam_prob, " %")

if __name__ == "__main__":
    main()