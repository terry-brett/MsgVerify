import numpy as np
import tensorflow as tf
from transformers import AutoTokenizer
import json

class Classifier():
    def __init__(self):
        pass

    def predict(self, text):
        with open('config.json') as f:
            config = json.load(f)
                
        TFLITE_PATH = config["text_classifier_model_path"]
        TOKENIZER_PATH = config["tokenizer_path"]

        MAX_LEN = 128

        tokenizer = AutoTokenizer.from_pretrained(TOKENIZER_PATH)
        interpreter = tf.lite.Interpreter(model_path=TFLITE_PATH)
        interpreter.allocate_tensors()

        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()

        input_map = {}

        for d in input_details:
            name = d["name"].lower()
            if "input_ids" in name:
                input_map["input_ids"] = d["index"]
            elif "attention_mask" in name:
                input_map["attention_mask"] = d["index"]

        assert "input_ids" in input_map
        assert "attention_mask" in input_map
        enc = tokenizer(
            text,
            padding="max_length",
            truncation=True,
            max_length=MAX_LEN,
            return_tensors="np"
        )

        input_ids = enc["input_ids"].astype(np.int32)
        attention_mask = enc["attention_mask"].astype(np.int32)

        # Sanity check: mask must not be all zeros
        if attention_mask.sum() == 0:
            raise ValueError("Attention mask is all zeros")

        interpreter.set_tensor(input_map["input_ids"], input_ids)
        interpreter.set_tensor(input_map["attention_mask"], attention_mask)

        interpreter.invoke()

        output = interpreter.get_tensor(output_details[0]["index"])

        # Detect whether softmax is already applied
        if output.max() <= 1.0 and output.min() >= 0.0:
            probs = output[0]
        else:
            probs = tf.nn.softmax(output, axis=-1).numpy()[0]

        return probs