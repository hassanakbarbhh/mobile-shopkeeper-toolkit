import re

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/catalog/OnlineCatalogFragment.kt", "r") as f:
    text = f.read()

onview_pattern = r'super\.onViewCreated\(view, savedInstanceState\)'
onview_replacement = """super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            OnlineCatalogRepository.fetchLiveModels()
            setupBrandChips()
            updateList()
        }"""
        
text = re.sub(onview_pattern, onview_replacement, text)

with open("app/src/main/java/com/shopkeeper/mobileshop/ui/catalog/OnlineCatalogFragment.kt", "w") as f:
    f.write(text)
