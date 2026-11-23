# PlantID - ML Engine

The machine-learning engine of **PlantID**, a mobile app that identifies plants from a photo and shows care tips.

| File | What it is |
|---|---|
| `Model_Base.ipynb` | Base model (Project Deliverable 2): MobileNetV2 on 7 species |
| `Model_Final.ipynb` | **Final model** (Project Deliverable 3): one notebook with every step, from data collection and cleaning to training, evaluation, explainability, TensorFlow Lite export and a demo. Saved with the outputs of the final training run |
| `FloraDB_Exploration.ipynb`, `care_tips.json`, `species_map.json` | Plant care data used by the app |
| `model/` | Files the Android app needs: `plantid_v1.tflite`, `labels.txt`, `classes.json`, `model_metadata.json` |

## Final model in short

* **30 species** common in Kenyan homes and gardens, plus an **"other plant"** answer for anything else.
* **28,969 photos** from Plant100-CLS and iNaturalist. Photos by the same photographer never appear in both
  training and test, so test scores reflect new users' photos.
* The model was **retrained**, not reused, because the number of species changed (7 → 30) and because the base
  model's data had a labelling error and near-duplicate photos (explained in `Model_Final.ipynb`, Sections 1, 4 and 5).
* **Final model: MobileNetV2** with an improved training recipe, chosen on validation data over MobileNetV3-Large,
  the base recipe, a CNN trained from scratch and logistic regression. EfficientNet-B0 was skipped because the run
  reached its GPU time budget.

| Test set (4,179 photos) | Result |
|---|---|
| Accuracy / macro-F1 / top-3 | 87.9% / 0.877 / 96.0% |
| Base recipe on the same data | 72.0% / 0.720 / 87.4% |
| Supported plants answered (rest: "not sure") | 86.5%, right 97.4% of the time when it answers |
| Never-seen species answered "not sure" | 78.5% |
| Phone file | 4.6 MB (float16), agrees with the full model on 99.8% of test photos |

## Using the model in the app

Input: one RGB photo, turned upright, centre-cropped to a square, resized to 224 × 224, values 0–255 as float32
(no normalisation; the model does it). Output: 31 probabilities in the order of `labels.txt`. To decide the answer,
apply the temperature and threshold in `model_metadata.json` (the exact rule is written there): if the top answer is
`other_plant` or below the threshold, show "Not sure which plant this is" with the top-3 guesses.

## For team members: running the notebook again

The dataset (`plantid_v1.zip`, about 1.9 GB) is not in the repository. Ask the team for the shared copy.
A full run takes about 4.5–5 hours on a free T4 GPU.

* **Colab:** put `plantid_v1.zip` in `MyDrive/PlantID/`, open `Model_Final.ipynb` (*File → Upload notebook*), choose
  *Runtime → Change runtime type → T4 GPU*, then *Run all*. Results are saved to `MyDrive/PlantID/outputs` and
  `MyDrive/PlantID/plantid_outputs.zip`.
* **Kaggle:** add the dataset to the notebook, turn on GPU and Internet, then *Save Version → Save & Run All*.
* For a quick test of the whole notebook (small subset, a few minutes), set the environment variable
  `PLANTID_QUICK=1` before running. The numbers from a quick run are not meaningful.
