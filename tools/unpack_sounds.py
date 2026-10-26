#!/usr/bin/env python3
import os
import json
import shutil

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    index_file = os.path.join(base_dir, "mcp_workspace/jars/assets/indexes/b1.7.json")
    objects_dir = os.path.join(base_dir, "mcp_workspace/jars/assets/objects")
    dest_dir = os.path.join(base_dir, "src/main/resources/resources")
    run_dest_dir = os.path.join(base_dir, "run/resources")

    if not os.path.exists(index_file):
        print(f"Index file {index_file} not found!")
        return

    with open(index_file, "r") as f:
        data = json.load(f)

    count = 0
    for section in ["objects", "custom"]:
        items = data.get(section, {})
        for rel_path, meta in items.items():
            h = meta.get("hash")
            if not h:
                continue
            src_file = os.path.join(objects_dir, h[:2], h)
            if not os.path.exists(src_file):
                continue
            
            for d in [dest_dir, run_dest_dir]:
                target_file = os.path.join(d, rel_path)
                os.makedirs(os.path.dirname(target_file), exist_ok=True)
                shutil.copyfile(src_file, target_file)
            count += 1

    print(f"Successfully extracted {count} audio resources into resources/")

if __name__ == "__main__":
    main()

