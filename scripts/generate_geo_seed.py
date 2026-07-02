#!/usr/bin/env python3
"""Generate Flyway seed SQL from countries/states JSON exports."""

from __future__ import annotations

import json
import sys
from pathlib import Path


def sql_str(value: str | None) -> str:
    if value is None:
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def sql_bool(value: int | bool | None) -> str:
    if value is None:
        return "FALSE"
    return "TRUE" if bool(value) else "FALSE"


def sql_decimal(value: str | float | int | None) -> str:
    if value is None or value == "":
        return "NULL"
    return str(value)


def main() -> int:
    if len(sys.argv) != 4:
        print(
            "Usage: generate_geo_seed.py <countries.json> <states.json> <output.sql>",
            file=sys.stderr,
        )
        return 1

    countries_path = Path(sys.argv[1])
    states_path = Path(sys.argv[2])
    output_path = Path(sys.argv[3])

    countries = json.loads(countries_path.read_text(encoding="utf-8"))["countries"]
    states = json.loads(states_path.read_text(encoding="utf-8"))["states"]

    lines: list[str] = [
        "-- Seed countries and states reference data",
        f"-- Generated from {countries_path.name} ({len(countries)} rows)",
        f"-- and {states_path.name} ({len(states)} rows)",
        "",
        "INSERT INTO countries (",
        "    id, name, iso3, iso2, numeric_code, phone_code, capital, currency,",
        "    currency_name, currency_symbol, tld, native, region, subregion,",
        "    latitude, longitude, emoji, has_states",
        ") VALUES",
    ]

    country_rows: list[str] = []
    for c in countries:
        country_rows.append(
            "("
            f"{c['id']}, {sql_str(c.get('name'))}, {sql_str(c.get('iso3'))}, "
            f"{sql_str(c.get('iso2'))}, {sql_str(c.get('numeric_code'))}, "
            f"{sql_str(c.get('phone_code'))}, {sql_str(c.get('capital'))}, "
            f"{sql_str(c.get('currency'))}, {sql_str(c.get('currency_name'))}, "
            f"{sql_str(c.get('currency_symbol'))}, {sql_str(c.get('tld'))}, "
            f"{sql_str(c.get('native'))}, {sql_str(c.get('region'))}, "
            f"{sql_str(c.get('subregion'))}, {sql_decimal(c.get('latitude'))}, "
            f"{sql_decimal(c.get('longitude'))}, {sql_str(c.get('emoji'))}, "
            f"{sql_bool(c.get('hasStates'))}"
            ")"
        )

    lines.append(",\n".join(country_rows))
    lines.append("ON CONFLICT (id) DO NOTHING;")
    lines.append("")
    lines.append("INSERT INTO states (")
    lines.append("    id, country_id, name, state_code, has_cities, latitude, longitude")
    lines.append(") VALUES")

    state_rows: list[str] = []
    for s in states:
        state_rows.append(
            "("
            f"{s['id']}, {s['country_id']}, {sql_str(s.get('name'))}, "
            f"{sql_str(s.get('state_code'))}, {sql_bool(s.get('hasCities'))}, "
            f"{sql_decimal(s.get('latitude'))}, {sql_decimal(s.get('longitude'))}"
            ")"
        )

    lines.append(",\n".join(state_rows))
    lines.append("ON CONFLICT (id) DO NOTHING;")
    lines.append("")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {output_path} ({len(countries)} countries, {len(states)} states)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
