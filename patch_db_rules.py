import json

with open('database.rules.json', 'r') as f:
    data = json.load(f)

member_rule = "auth != null && (root.child('shops').child($shopId).child('ownerUid').val() == auth.uid || root.child('shops').child($shopId).child('members').child(auth.uid).exists())"

if 'shops' in data['rules'] and '$shopId' in data['rules']['shops']:
    shop = data['rules']['shops']['$shopId']
    for key, value in shop.items():
        if isinstance(value, dict):
            if '.read' in value:
                value['.read'] = member_rule
            if '.write' in value:
                value['.write'] = member_rule

with open('database.rules.json', 'w') as f:
    json.dump(data, f, indent=2)
