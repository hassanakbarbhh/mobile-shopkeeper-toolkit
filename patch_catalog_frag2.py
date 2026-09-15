import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/catalog/OnlineCatalogFragment.kt", "r") as f:
    text = f.read()

onview_pattern = r'viewLifecycleOwner\.lifecycleScope\.launch \{.*?OnlineCatalogRepository\.fetchLiveModels\(\).*?setupBrandChips\(\).*?updateList\(\).*?\}'
onview_replacement = """setupBrandChips()
        updateList()"""

text = re.sub(onview_pattern, onview_replacement, text, flags=re.DOTALL)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/catalog/OnlineCatalogFragment.kt", "w") as f:
    f.write(text)
