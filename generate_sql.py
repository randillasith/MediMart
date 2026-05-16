import random
from datetime import datetime, timedelta

categories = [
    (1, 'Pain Relief', 'ACTIVE'),
    (2, 'Antibiotics', 'ACTIVE'),
    (3, 'Vitamins & Supplements', 'ACTIVE'),
    (4, 'Cold & Flu', 'ACTIVE'),
    (5, 'First Aid', 'ACTIVE'),
    (6, 'Digestive Health', 'ACTIVE'),
    (7, 'Skin Care', 'ACTIVE'),
    (8, 'Eye & Ear Care', 'ACTIVE'),
    (9, 'Heart Health', 'ACTIVE'),
    (10, 'Diabetes Care', 'ACTIVE')
]

brands = ['GSK', 'Pfizer', 'Novartis', 'Bayer', 'Johnson & Johnson', 'Sanofi', 'Merck', 'Abbott', 'Roche', 'AstraZeneca', 'Local Pharma', 'Generic']
names = ['Panadol', 'Amoxicillin', 'Vitamin C', 'Aspirin', 'Ibuprofen', 'Cetirizine', 'Loratadine', 'Omeprazole', 'Metformin', 'Atorvastatin', 'Amlodipine', 'Levothyroxine', 'Azithromycin', 'Clopidogrel', 'Losartan', 'Pantoprazole']
dosages = ['100mg', '250mg', '500mg', '1000mg', '10ml', '50ml', '100ml', '2 drops', '5mg', '10mg', '20mg']
statuses = ['AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED']
form_types = ['TABLET', 'SYRUP', 'CREAM', 'OTHER']

sql_statements = []

sql_statements.append('SET FOREIGN_KEY_CHECKS = 0;')
sql_statements.append('TRUNCATE TABLE medicines;')
sql_statements.append('TRUNCATE TABLE categories;')
sql_statements.append('SET FOREIGN_KEY_CHECKS = 1;')
sql_statements.append('')

for c in categories:
    sql_statements.append(f"INSERT INTO categories (id, name, status) VALUES ({c[0]}, '{c[1]}', '{c[2]}');")

sql_statements.append('')

for i in range(1, 501):
    id = i
    dtype = random.choice(['OTC', 'PRESCRIPTION'])
    brand = random.choice(brands)
    category_id = random.randint(1, 10)
    dosage = random.choice(dosages)
    suffix = random.choice(['Plus', 'Extra', 'Forte', 'Basic', ''])
    base_name = random.choice(names)
    name = f"{base_name} {suffix}".strip()
    
    days_to_add = random.randint(365, 1825)
    expiry_date = (datetime.now() + timedelta(days=days_to_add)).strftime('%Y-%m-%d')
    
    prescription_required = 1 if dtype == 'PRESCRIPTION' else 0
    price = round(random.uniform(5.0, 150.0), 2)
    sku = f'MED{i:03d}'
    status = random.choice(statuses)
    form_type = random.choice(form_types)
    stock_qty = random.randint(0, 500) if status != 'OUT_OF_STOCK' else 0
    if stock_qty == 0 and status == 'AVAILABLE':
        status = 'OUT_OF_STOCK'
    elif stock_qty > 0 and stock_qty <= 100 and status == 'AVAILABLE':
        status = 'LOW_STOCK'
        
    sql_statements.append(f"INSERT INTO medicines (id, dtype, brand, category_id, dosage, form_type, expiry_date, name, prescription_required, price, sku, status, stock_qty, image_url) VALUES ({id}, '{dtype}', '{brand}', {category_id}, '{dosage}', '{form_type}', '{expiry_date}', '{name}', {prescription_required}, {price}, '{sku}', '{status}', {stock_qty}, NULL);")

with open('test_data.sql', 'w') as f:
    f.write('\n'.join(sql_statements))
print('test_data.sql generated successfully!')
