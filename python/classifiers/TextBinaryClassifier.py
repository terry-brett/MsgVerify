import numpy as np
import tensorflow as tf
from transformers import AutoTokenizer
import json

class Classifier():
    def __init__(self):
        with open('config.json') as f:
            config = json.load(f)
                
        self.TFLITE_PATH = config["text_classifier_model_path"]
        self.TOKENIZER_PATH = config["tokenizer_path"]

        self.MAX_LEN = 128

        self.tokenizer = AutoTokenizer.from_pretrained(self.TOKENIZER_PATH)
        self.interpreter = tf.lite.Interpreter(model_path=self.TFLITE_PATH)
        self.interpreter.allocate_tensors()

        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()

        self.input_map = {}

        for d in self.input_details:
            name = d["name"].lower()
            if "input_ids" in name:
                self.input_map["input_ids"] = d["index"]
            elif "attention_mask" in name:
                self.input_map["attention_mask"] = d["index"]

        assert "input_ids" in self.input_map
        assert "attention_mask" in self.input_map
        

    def predict(self, text):
        
        enc = self.tokenizer(
            text,
            padding="max_length",
            truncation=True,
            max_length=self.MAX_LEN,
            return_tensors="np"
        )

        self.input_ids = enc["input_ids"].astype(np.int32)
        self.attention_mask = enc["attention_mask"].astype(np.int32)

        # Sanity check: mask must not be all zeros
        if self.attention_mask.sum() == 0:
            raise ValueError("Attention mask is all zeros")

        self.interpreter.set_tensor(self.input_map["input_ids"], self.input_ids)
        self.interpreter.set_tensor(self.input_map["attention_mask"], self.attention_mask)

        self.interpreter.invoke()

        output = self.interpreter.get_tensor(self.output_details[0]["index"])

        # Detect whether softmax is already applied
        if output.max() <= 1.0 and output.min() >= 0.0:
            probs = output[0]
        else:
            probs = tf.nn.softmax(output, axis=-1).numpy()[0]

        return probs