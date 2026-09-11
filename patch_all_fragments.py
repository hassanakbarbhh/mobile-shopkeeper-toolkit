import os
import re

for root, _, files in os.walk("app/src/main/java/com/shopkeeper/mobileshop/ui/"):
    for file in files:
        if file.endswith("Fragment.kt"):
            path = os.path.join(root, file)
            with open(path, "r") as f:
                content = f.read()
            
            # replace binding. with _binding?. inside launch { ... collect ... }
            # Actually, doing this globally inside the file is safer if it's inside a coroutine
            # It's hard to precisely match, but let's replace all `binding.` with `_binding?.` 
            # if they appear inside a collect block. Or I can just check if _binding != null before collecting.
            # But wait, simpler is to just do a regex replace for the known UI elements in collect blocks in these files.
            
            # For SellersFragment
            if file == "SellersFragment.kt":
                content = content.replace("binding.layoutEmpty.visibility", "_binding?.layoutEmpty?.visibility")
                content = content.replace("binding.tvStaffSummary.text", "_binding?.tvStaffSummary?.text")
            # For RepairsFragment
            if file == "RepairsFragment.kt":
                pass # allRepairs is updated, filterRepairs uses binding. Let's see filterRepairs.
                content = content.replace("binding.chipGroupStatus.checkedChipId", "(_binding?.chipGroupStatus?.checkedChipId ?: -1)")
            
            # For others, I can just leave it as is if there were no reports, but let's be careful.
            with open(path, "w") as f:
                f.write(content)
