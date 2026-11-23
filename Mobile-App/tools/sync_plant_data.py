#!/usr/bin/env python3
"""Generate the app's bundled plant data from the ML team's files.

Reads (never modifies):
  model/classes.json   the classifier's classes: index, slug (label), names, category
  model/labels.txt     the model's output order (checked against classes.json)
  care_tips.json       care values from the FloraDB sample, keyed by scientific name

Writes:
  Mobile-App/app/src/main/assets/plants.json  (schema version 1)

Run from the repository root after the ML team changes any of these files
(for example, when species are added):
  python Mobile-App/tools/sync_plant_data.py

Rules (DECISIONS D19-D21, D36):
  * Every model class except "other_plant" becomes a plant, keyed by its label.
  * Care values are linked only on an EXACT scientific-name match. Genus-level
    classes (e.g. "Bougainvillea spp.") are not matched to one species' data.
  * Toxicity fields (is_toxic_to_dogs, is_toxic_to_cats, toxicity_symptoms) are
    never copied. The spec forbids toxicity claims without a reviewed source.

The script fails instead of guessing when data is missing or inconsistent.
Output is deterministic (fixed order and formatting) so diffs stay reviewable.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

SCHEMA_VERSION = 1

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_CLASSES = REPO_ROOT / "model" / "classes.json"
DEFAULT_LABELS = REPO_ROOT / "model" / "labels.txt"
DEFAULT_CARE_TIPS = REPO_ROOT / "care_tips.json"
DEFAULT_OUTPUT = REPO_ROOT / "Mobile-App" / "app" / "src" / "main" / "assets" / "plants.json"

OTHER_PLANT = "other_plant"

# Licence checked on the dataset card on 2026-11-19. Re-check if the source changes.
SOURCE = {
    "name": "FloraDB houseplant care sample",
    "url": "https://huggingface.co/datasets/Ichlibitiche/floradb-houseplants-care-sample",
    "license": "CC BY-NC 4.0",
}
EXPECTED_SOURCE_MARKER = "floradb-houseplants-care-sample"

EXCLUDED_FIELDS = {"is_toxic_to_dogs", "is_toxic_to_cats", "toxicity_symptoms"}
LABEL = re.compile(r"^[a-z][a-z0-9_]*$")


class DataError(Exception):
    pass


def whole_number(entry: dict, key: str, species: str) -> int | None:
    value = entry.get(key)
    if value is None:
        return None
    if isinstance(value, bool) or not isinstance(value, (int, float)) or float(value) != int(value):
        raise DataError(f"{species}: '{key}' must be a whole number, got {value!r}")
    return int(value)


def value_range(entry: dict, low_key: str, high_key: str, species: str) -> dict | None:
    low, high = whole_number(entry, low_key, species), whole_number(entry, high_key, species)
    if low is None and high is None:
        return None
    if low is None or high is None:
        raise DataError(f"{species}: '{low_key}' and '{high_key}' must both be present")
    if low > high:
        raise DataError(f"{species}: '{low_key}' ({low}) is greater than '{high_key}' ({high})")
    return {"min": low, "max": high}


def care_values(entry: dict, species: str) -> dict:
    if EXPECTED_SOURCE_MARKER not in str(entry.get("source", "")):
        raise DataError(f"{species}: unexpected care source {entry.get('source')!r}; update SOURCE first")
    care = {
        "lightLux": value_range(entry, "min_lux", "max_lux", species),
        "temperatureCelsius": value_range(entry, "min_temp_celsius", "max_temp_celsius", species),
        "humidityPercent": whole_number(entry, "ideal_humidity_percent", species),
        "wateringIntervalDays": whole_number(entry, "watering_frequency_days", species),
    }
    return {k: v for k, v in care.items() if v is not None}


def build(classes: list, labels: list, care_tips: dict) -> tuple[dict, list]:
    if [c.get("slug") for c in sorted(classes, key=lambda c: c["index"])] != labels:
        raise DataError("classes.json slugs (by index) don't match labels.txt order")

    plants, without_care = [], []
    for entry in sorted(classes, key=lambda c: c["index"]):
        label = entry["slug"]
        if not LABEL.match(label):
            raise DataError(f"unexpected label {label!r}")
        if label == OTHER_PLANT:
            continue
        scientific = entry.get("scientific")
        scientific = None if scientific in (None, "", "-") else scientific
        care_entry = care_tips.get(scientific) if scientific else None
        plant = {
            "id": label,
            "classLabel": label,
            "scientificName": scientific,
            "commonName": entry.get("common"),
            "category": entry.get("category"),
        }
        if care_entry is not None:
            plant["family"] = care_entry.get("family")
            plant["care"] = care_values(care_entry, scientific)
        else:
            without_care.append(label)
        plants.append(plant)

    if not plants:
        raise DataError("no plants found in classes.json")
    data = {
        "schemaVersion": SCHEMA_VERSION,
        "generatedBy": "Mobile-App/tools/sync_plant_data.py from model/classes.json and care_tips.json",
        "excludedFields": sorted(EXCLUDED_FIELDS),
        "source": SOURCE,
        "plants": plants,
    }
    return data, without_care


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--classes", type=Path, default=DEFAULT_CLASSES)
    parser.add_argument("--labels", type=Path, default=DEFAULT_LABELS)
    parser.add_argument("--care-tips", type=Path, default=DEFAULT_CARE_TIPS)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    try:
        classes = json.loads(args.classes.read_text(encoding="utf-8"))
        labels = [line.strip() for line in args.labels.read_text(encoding="utf-8").splitlines() if line.strip()]
        care_tips = json.loads(args.care_tips.read_text(encoding="utf-8"))
        data, without_care = build(classes, labels, care_tips)
    except (OSError, json.JSONDecodeError, KeyError, DataError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    with_care = len(data["plants"]) - len(without_care)
    print(f"wrote {len(data['plants'])} plants ({with_care} with care values) to {args.output}")
    if without_care:
        print(f"no exact care match (care guide shows 'not available'): {', '.join(without_care)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
