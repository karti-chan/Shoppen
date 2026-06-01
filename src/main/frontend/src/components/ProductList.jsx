import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'

function ProductList() {
  const [products, setProducts] = useState([])
  const [filteredProducts, setFilteredProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const { addToCart } = useCart() // ✅ Hook z CartContext

  const [categories, setCategories] = useState([])
  const [selectedCategories, setSelectedCategories] = useState([])
  const [priceRange, setPriceRange] = useState({ min: 0, max: 100 })
  const [sortOption, setSortOption] = useState('')

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)


        const productsResponse = await fetch('http://localhost:8080/api/products')
        const productsData = await productsResponse.json()
        console.log('📦 Otrzymane produkty:', productsData)

        setProducts(productsData)
        setFilteredProducts(productsData)

        const categoriesResponse = await fetch('http://localhost:8080/api/products/categories')
        const categoriesData = await categoriesResponse.json()
        console.log('🏷️ Kategorie:', categoriesData)

        setCategories(categoriesData)

      } catch (error) {
        console.error('❌ Błąd:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])


  const applyFilters = () => {
    let filtered = [...products]

    if (selectedCategories.length > 0) {
      filtered = filtered.filter(product =>
        product.category && selectedCategories.includes(product.category)
      )
    }


    filtered = filtered.filter(product =>
      product.price >= priceRange.min && product.price <= priceRange.max
    )


    if (sortOption) {
      filtered.sort((a, b) => {
        switch(sortOption) {
          case 'price-asc':
            return a.price - b.price
          case 'price-desc':
            return b.price - a.price
          case 'name-asc':
            return a.productName.localeCompare(b.productName)
          case 'name-desc':
            return b.productName.localeCompare(a.productName)
          default:
            return 0
        }
      })
    }

    console.log('🔍 Przefiltrowane produkty:', filtered.length)
    setFilteredProducts(filtered)
  }


  useEffect(() => {
    applyFilters()
  }, [selectedCategories, priceRange, sortOption, products])


  const handleCategoryChange = (category) => {
    setSelectedCategories(prev => {
      if (prev.includes(category)) {
        return prev.filter(c => c !== category)
      } else {
        return [...prev, category]
      }
    })
  }


  const handlePriceChange = (type, value) => {
    const numValue = Number(value)
    setPriceRange(prev => ({
      ...prev,
      [type]: numValue
    }))
  }


  const handleSortChange = (option) => {
    setSortOption(option)
  }


  const clearFilters = () => {
    setSelectedCategories([])
    setPriceRange({ min: 0, max: 100 })
    setSortOption('')
  }

  const handleAddToCart = async (product) => {
    try {
      console.log(`🛒 Dodawanie do koszyka:`, product);
      console.log(`🛒 Product ID: ${product.productId}, Name: ${product.productName}`);


      const token = localStorage.getItem('token');
      console.log('🔑 Token exists?', !!token);

      if (!token) {
        alert('Musisz się zalogować, aby dodawać produkty do koszyka!');
        return;
      }


      const success = await addToCart(product.productId, 1);

      if (success) {
        alert('✅ Produkt dodany do koszyka!');
      } else {
        alert('❌ Nie udało się dodać do koszyka. Sprawdź konsolę.');
      }
    } catch (error) {
      console.error('❌ Error in handleAddToCart:', error);
      alert('Wystąpił błąd: ' + error.message);
    }
  };

  if (loading) {
    return <div className="loading">Ładowanie...</div>
  }

  return (
    <div className="container">
      {/* Filtry */}
      <aside className="filters-sidebar">
        <h2>FILTRY</h2>

        <button
          onClick={clearFilters}
          className="clear-filters-btn"
          style={{
            marginBottom: '20px',
            padding: '8px 15px',
            backgroundColor: '#ff6b6b',
            color: 'white',
            border: 'none',
            borderRadius: '5px',
            cursor: 'pointer'
          }}
        >
          🗑️ Wyczyść wszystkie filtry
        </button>

        {/* Filtry kategorii */}
        <div className="filter-section">
          <h3>Kategoria</h3>
          {categories.length > 0 ? (
            categories.map(category => (
              <label key={category} className="category-checkbox">
                <input
                  type="checkbox"
                  checked={selectedCategories.includes(category)}
                  onChange={() => handleCategoryChange(category)}
                />
                <span>{category}</span>
              </label>
            ))
          ) : (
            <p>Brak kategorii</p>
          )}
        </div>

        {/* Filtr ceny */}
        <div className="filter-section">
          <h3>Cena</h3>
          <div className="price-inputs">
            <input
              type="number"
              value={priceRange.min}
              onChange={(e) => handlePriceChange('min', e.target.value)}
              min="0"
              max="100"
            />
            <span>-</span>
            <input
              type="number"
              value={priceRange.max}
              onChange={(e) => handlePriceChange('max', e.target.value)}
              min="0"
              max="100"
            />
            <span>zł</span>
          </div>

          <div className="price-slider" style={{ marginTop: '10px' }}>
            <div style={{ display: 'flex', gap: '10px' }}>
              <input
                type="range"
                min="0"
                max="100"
                value={priceRange.min}
                onChange={(e) => handlePriceChange('min', e.target.value)}
                style={{ width: '100%' }}
              />
              <input
                type="range"
                min="0"
                max="100"
                value={priceRange.max}
                onChange={(e) => handlePriceChange('max', e.target.value)}
                style={{ width: '100%' }}
              />
            </div>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              marginTop: '5px',
              fontSize: '12px',
              color: '#666'
            }}>
              <span>{priceRange.min} zł</span>
              <span>{priceRange.max} zł</span>
            </div>
          </div>
        </div>

        {/* Sortowanie */}
        <div className="filter-section">
          <h3>Sortuj według</h3>
          <select
            value={sortOption}
            onChange={(e) => handleSortChange(e.target.value)}
            style={{
              width: '100%',
              padding: '8px',
              borderRadius: '5px',
              border: '1px solid #ddd'
            }}
          >
            <option value="">Domyślnie</option>
            <option value="price-asc">Cena: od najtańszych</option>
            <option value="price-desc">Cena: od najdroższych</option>
            <option value="name-asc">Nazwa: A-Z</option>
            <option value="name-desc">Nazwa: Z-A</option>
          </select>
        </div>

        {/* Statystyki filtrów */}
        <div className="filter-stats" style={{
          marginTop: '20px',
          padding: '10px',
          backgroundColor: '#f0f8ff',
          borderRadius: '5px',
          fontSize: '12px'
        }}>
          <p><strong>Aktywne filtry:</strong></p>
          <p>Kategorie: {selectedCategories.length > 0 ? selectedCategories.join(', ') : 'wszystkie'}</p>
          <p>Cena: {priceRange.min} - {priceRange.max} zł</p>
          <p>Sortowanie: {
            sortOption === 'price-asc' ? 'Cena rosnąco' :
            sortOption === 'price-desc' ? 'Cena malejąco' :
            sortOption === 'name-asc' ? 'Nazwa A-Z' :
            sortOption === 'name-desc' ? 'Nazwa Z-A' : 'domyślnie'
          }</p>
        </div>
      </aside>

      {/* Produkty */}
      <main className="products-main">
        <div className="products-header">
          <div className="results-count">
            Znalezione produkty: <strong>{filteredProducts.length}</strong>
            {filteredProducts.length !== products.length && (
              <span style={{ color: '#666', marginLeft: '10px' }}>
                (przefiltrowano z {products.length})
              </span>
            )}
          </div>
        </div>

        <div className="products-grid">
          {filteredProducts.map(product => (
            <div key={product.productId} className="product-card">
              <div className="product-image">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt={product.productName} />
                ) : (
                  <>
                    {product.productName === 'Marchew' && '🥕'}
                    {product.productName === 'Ser' && '🧀'}
                    {product.productName === 'Jajko' && '🥚'}
                    {product.productName === 'Szynka' && '🍖'}
                  </>
                )}
              </div>

              <div className="product-info">
                <Link to={`/product/${product.productId}`} className="product-link">
                  <h3 className="product-name">{product.productName}</h3>
                </Link>

                <div className="price-section">
                  <span className="current-price">{product.price} zł</span>
                </div>

                {product.category && (
                  <div className="product-category">Kategoria: {product.category}</div>
                )}

                <div className="product-actions">
                  {/* ✅ ✅ ✅ PRZYCISK DODAJ DO KOSZYKA - TERAZ DZIAŁA ✅ ✅ ✅ */}
                  <button
                    className="add-to-cart-btn"
                    onClick={() => handleAddToCart(product)}
                  >
                    🛒 Dodaj do koszyka
                  </button>

                  <Link to={`/product/${product.productId}`} className="details-btn">
                    ℹ️ Szczegóły
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Komunikat jeśli brak produktów */}
        {filteredProducts.length === 0 && (
          <div style={{
            textAlign: 'center',
            padding: '40px',
            color: '#666'
          }}>
            <h3>😕 Nie znaleziono produktów</h3>
            <p>Spróbuj zmienić kryteria wyszukiwania</p>
            <button
              onClick={clearFilters}
              style={{
                marginTop: '20px',
                padding: '10px 20px',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer'
              }}
            >
              Wyczyść wszystkie filtry
            </button>
          </div>
        )}
      </main>
    </div>
  )
}

export default ProductList