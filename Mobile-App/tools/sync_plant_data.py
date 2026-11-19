#!/usr/bin/env python3
"""Generate the app's bundled plant data from the AI team's JSON files.

Reads (never modifies):
  species_map.json  classifier class label -> scientific name
  care_tips.json    scientific name -> care values from the FloraDB sample

Writes:
  Mobile-App/app/src/main/assets/plants.json  (schema version 1)

Run from the repository root after the AI team changes either file:
  python Mobile-App/tools/sync_plant_data.py

Deliberately NOT copied (DECISIONS.md D19-D21):
  is_toxic_to_dogs, is_toxic_to_cats, toxicity_symptoms. The spec forbids
  toxicity claims without a reviewed source and an agreed scope.

The script fails instead of guessing when data is missing or malformed.
Output is deterministic (sorted, fixed formatting) so diffs stay reviewable.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

SCHEMA_VERSION = 1

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SPECIES_MAP = REPO_ROOT / "species_map.json"
DEFAULT_CARE_TIPS = REPO_ROOT / "care_tips.json"
DEFAULT_OUTPUT = REPO_ROOT / "Mobile-App" / "app" / "src" / "main" / "assets" / "plants.json"

# Licence checked on the dataset card on 2026-11-19. Re-check if the source changes.
SOURCE = {
    "name": "FloraDB houseplant care sample",
    "url": "https://huggingface.co/datasets/Ichlibitiche/floradb-houseplants-care-sample",
    "license": "CC BY-NC 4.0",
}
EXPECTED_SOURCE_MARKER = "floradb-houseplants-care-sample"

EXCLUDED_FIELDS = {"is_toxic_to_dogs", "is_toxic_to_cats", "toxicity_symptoms"}
CLASS_LABEL = re.compile(r"^[A-Za-z][A-Za-z0-9_]*$")


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


def build(species_map: dict, care_tips: dict) -> dict:
    plants = []
    for class_label, scientific_name in sorted(species_map.items()):
        if not CLASS_LABEL.match(class_label):
            raise DataError(f"unexpected class label {class_label!r}")
        entry = care_tips.get(scientific_name)
        if entry is None:
            raise DataError(f"{scientific_name} (class {class_label}) has no entry in care_tips.json")
        if EXPECTED_SOURCE_MARKER not in str(entry.get("source", "")):
            raise DataError(f"{scientific_name}: unexpected source {entry.get('source')!r}; update SOURCE first")

        care = {
            "lightLux": value_range(entry, "min_lux", "max_lux", scientific_name),
            "temperatureCelsius": value_range(entry, "min_temp_celsius", "max_temp_celsius", scientific_name),
            "humidityPercent": whole_number(entry, "ideal_humidity_percent", scientific_name),
            "wateringIntervalDays": whole_number(entry, "watering_frequency_days", scientific_name),
        }
        plants.append({
            "id": class_label.lower(),
            "classLabel": class_label,
            "scientificName": scientific_name,
            "commonName": entry.get("common_name"),
            "family": entry.get("family"),
            "care": {k: v for k, v in care.items() if v is not None},
        })

    unused = sorted(set(care_tips) - set(species_map.values()))
    if unused:
        print(f"note: care_tips.json entries without a classifier class (skipped): {unused}", file=sys.stderr)

    return {
        "schemaVersion": SCHEMA_VERSION,
        "generatedBy": "Mobile-App/tools/sync_plant_data.py from species_map.json and care_tips.json",
        "excludedFields": sorted(EXCLUDED_FIELDS),
        "source": SOURCE,
        "plants": plants,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--species-map", type=Path, default=DEFAULT_SPECIES_MAP)
    parser.add_argument("--care-tips", type=Path, default=DEFAULT_CARE_TIPS)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    try:
        species_map = json.loads(args.species_map.read_text(encoding="utf-8"))
        care_tips = json.loads(args.care_tips.read_text(encoding="utf-8"))
        data = build(species_map, care_tips)
    except (OSError, json.JSONDecodeError, DataError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print(f"wrote {len(data['plants'])} plants to {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
