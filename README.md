# PlantID — ML Engine

The machine-learning engine of **PlantID**, a mobile app that identifies plants from a photo and shows care tips.

| File | What it is |
|---|---|
| `Model_Base.ipynb` | Base model (Project Deliverable 2): MobileNetV2 on 7 species |
| `Model_Final.ipynb` | **Final model** (Project Deliverable 3): one notebook with every step, from data collection and cleaning to training, evaluation, explainability, TensorFlow Lite export and a demo |
| `FloraDB_Exploration.ipynb`, `care_tips.json`, `species_map.json` | Plant care data used by the app |
| `model/` | Files the Android app needs: `plantid_v1.tflite`, `labels.txt`, `classes.json`, `model_metadata.json` (added after the final training run) |

## Final model in short

* **30 species** common in Kenyan homes and gardens, plus an **"other plant"** answer for anything else.
* **28,969 photos** from Plant100-CLS and iNaturalist. Photos by the same photographer never appear in both
  training and test, so test scores reflect new users' photos.
* The model was **retrained**, not reused, because the number of species changed (7 → 30) and because the base
  model's data had a labelling error and near-duplicate photos (explained in `Model_Final.ipynb`, Sections 1, 4 and 5).
* Transfer learning with phone-sized networks (MobileNetV2, MobileNetV3, EfficientNet-B0), compared against the
  base recipe, a CNN trained from scratch and logistic regression.

## Running the notebook

* **Kaggle:** add the `plantid-v1` dataset, enable GPU and Internet, *Save Version → Save & Run All*.
* **Colab:** place `plantid_v1.zip` in `MyDrive/PlantID/`, choose a GPU runtime, *Run all*.

## Using the model in the app

Input: one RGB photo, turned upright, centre-cropped to a square, resized to 224 × 224, values 0–255 as float32
(no normalisation; the model does it). Output: 31 probabilities in the order of `labels.txt`. If the top answer is
`other_plant` or below the confidence threshold in `model_metadata.json`, show "Not sure which plant this is".
