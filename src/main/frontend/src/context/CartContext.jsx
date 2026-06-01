import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useAuth } from './AuthContext';

const CartContext = createContext();

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
    const [cart, setCart] = useState({ items: [] });
    const [loading, setLoading] = useState(true);
    const [cartCount, setCartCount] = useState(0);
    const { isAuthenticated } = useAuth();

    console.log('🎯 CartProvider rendering - isAuthenticated:', isAuthenticated);

    // Funkcja do pobierania nagłówków z tokenem - POPRAWIONA
    const getAuthHeaders = () => {
        // ✅ SPRÓBUJ WSZYSTKIE MOŻLIWE NAZWY TOKENA
        const token = localStorage.getItem('token') ||
                      localStorage.getItem('accessToken') ||
                      localStorage.getItem('jwtToken');

        console.log('🔑 Token search result:',
                    token ? token.substring(0, 20) + '...' : 'NO TOKEN FOUND');

        // Debug: pokaż wszystkie klucze w localStorage
        if (!token) {
            console.log('🔑 Available in localStorage:');
            for (let i = 0; i < localStorage.length; i++) {
                const key = localStorage.key(i);
                console.log(`  - ${key}: ${localStorage.getItem(key)?.substring(0, 50)}...`);
            }
        }

        if (!token) {
            console.warn('⚠️ No token found in localStorage!');
            return {
                'Content-Type': 'application/json'
            };
        }

        return {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
    };

    // Pobierz cały koszyk - UŻYJ useCallback!
    const fetchCart = useCallback(async () => {
        try {
            const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
            console.log('🔄 fetchCart - Token exists?', !!token);
            console.log('🔄 fetchCart - isAuthenticated:', isAuthenticated);

            if (!token || !isAuthenticated) {
                console.log('🔄 No token or not authenticated');

                // SPRAWDŹ CACHE nawet jeśli nie ma tokena
                const cachedCart = localStorage.getItem('cart_cache');
                if (cachedCart) {
                    console.log('🔄 Using cached cart (no auth)');
                    const parsedCart = JSON.parse(cachedCart);
                    setCart(parsedCart);

                    // Oblicz licznik z cache
                    if (parsedCart.items) {
                        const count = parsedCart.items.reduce(
                            (sum, item) => sum + (item.quantity || 1), 0
                        );
                        setCartCount(count);
                    }

                    setLoading(false);
                    return;
                } else {
                    setCart({ items: [] });
                    setCartCount(0);
                    setLoading(false);
                    return;
                }
            }

            console.log('🔄 Fetching cart from API...');
            const response = await axios.get('http://localhost:8080/api/cart', {
                headers: getAuthHeaders()
            });

            console.log('🔄 Cart API response:', response.data);

            if (response.data && response.data.items) {
                setCart(response.data);
                // UŻYJ cartCount Z API jeśli istnieje, LUB oblicz
                const apiCount = response.data.totalItems || response.data.count;
                if (apiCount !== undefined) {
                    console.log('🔄 Using API cart count:', apiCount);
                    setCartCount(apiCount);
                } else {
                    // Oblicz ręcznie
                    const calculatedCount = response.data.items.reduce(
                        (sum, item) => sum + (item.quantity || 1), 0
                    );
                    console.log('🔄 Calculated cart count:', calculatedCount);
                    setCartCount(calculatedCount);
                }
            } else {
                console.log('🔄 API returned empty cart');

                // SPRAWDŹ CACHE jeśli API zwraca pusty
                const cachedCart = localStorage.getItem('cart_cache');
                if (cachedCart) {
                    console.log('🔄 Using cached cart (API empty)');
                    const parsedCart = JSON.parse(cachedCart);
                    setCart(parsedCart);

                    if (parsedCart.items) {
                        const count = parsedCart.items.reduce(
                            (sum, item) => sum + (item.quantity || 1), 0
                        );
                        setCartCount(count);
                    }
                } else {
                    setCart({ items: [] });
                    setCartCount(0);
                }
            }
        } catch (error) {
            console.error('❌ Error fetching cart:', error);

            // W PRZYPADKU BŁĘDU - użyj cache
            const cachedCart = localStorage.getItem('cart_cache');
            if (cachedCart) {
                console.log('🔄 Using cached cart due to API error');
                const parsedCart = JSON.parse(cachedCart);
                setCart(parsedCart);

                if (parsedCart.items) {
                    const count = parsedCart.items.reduce(
                        (sum, item) => sum + (item.quantity || 1), 0
                    );
                    setCartCount(count);
                }
            } else {
                setCart({ items: [] });
                setCartCount(0);
            }

            if (error.response?.status === 401) {
                console.log('❌ Unauthorized - clearing cart');
            }
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    // ================== NOWA POPRAWIONA FUNKCJA addToCart ==================
    const addToCart = async (productId, quantity = 1) => {
        console.log('🛒🛒🛒 CartContext.addToCart CALLED');
        console.log('📦 productId:', productId, 'type:', typeof productId);

        // 1. Pobierz token - SPRÓBUJ WSZYSTKIE MOŻLIWE NAZWY
        const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
        console.log('🔑 Token from localStorage (first 30 chars):',
                    token ? token.substring(0, 30) + '...' : 'NO TOKEN');

        if (!token) {
            console.error('❌ No token found! User not logged in.');
            alert('Musisz być zalogowany, aby dodać produkt do koszyka!');
            return false;
        }

        // 2. Sprawdź czy token ma poprawny format JWT
        if (!token.startsWith('eyJ')) {
            console.error('❌ Token format error! Token should start with "eyJ"');
            console.error('Actual token:', token);
            alert('Błąd autentykacji. Zaloguj się ponownie.');
            localStorage.removeItem('token');
            localStorage.removeItem('accessToken');
            return false;
        }

        // 3. Użyj QUERY PARAMS w URL zamiast params w config
        const url = `http://localhost:8080/api/cart/add?productId=${productId}&quantity=${quantity}`;
        console.log('📤 Request URL:', url);

        try {
            console.log('➕ Sending POST request to cart...');

            const response = await axios.post(
                url, // ← URL z query params
                {}, // pusty body
                {
                    headers: getAuthHeaders() // ← Użyj funkcji getAuthHeaders
                }
            );

            console.log('✅ Response status:', response.status);
            console.log('✅ Response data:', response.data);

            // Aktualizuj stan koszyka
            setCart(response.data);

            // Oblicz nowy licznik
            if (response.data.items) {
                const newCount = response.data.items.reduce(
                    (sum, item) => sum + (item.quantity || 1), 0
                );
                setCartCount(newCount);
                console.log('✅ Updated cart count:', newCount);
            }

            alert('✅ Produkt dodany do koszyka!');
            return true;

        } catch (error) {
            console.error('❌ ERROR adding to cart:');

            // Błąd 401 - nieautoryzowany
            if (error.response?.status === 401) {
                console.error('❌ 401 Unauthorized - invalid or expired token');
                alert('Sesja wygasła. Zaloguj się ponownie.');
                localStorage.removeItem('token');
                localStorage.removeItem('accessToken');
                return false;
            }

            // Błąd 403 - zabroniony
            if (error.response?.status === 403) {
                console.error('❌ 403 Forbidden');
                alert('Nie masz uprawnień do tej operacji.');
                return false;
            }

            // Inne błędy
            console.error('Status:', error.response?.status);
            console.error('Data:', error.response?.data);
            console.error('Headers:', error.response?.headers);

            alert(`Błąd: ${error.response?.data?.message || error.message}`);
            return false;
        }
    };
    // ================== KONIEC NOWEJ FUNKCJI addToCart ==================

    // Usuń z koszyka - POPRAWIONE
    const removeFromCart = async (productId) => {
        try {
            const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
            if (!token) {
                console.error('❌ No token for removeFromCart');
                return false;
            }

            // UWAGA: Backend używa @RequestParam, więc musimy użyć query params
            const url = `http://localhost:8080/api/cart/remove?productId=${productId}`;
            console.log(`➖ Removing product ${productId} from cart...`);

            const response = await axios.delete(url, {
                headers: getAuthHeaders()
            });

            setCart(response.data);

            // Aktualizuj licznik
            if (response.data.totalItems !== undefined) {
                setCartCount(response.data.totalItems);
            } else if (response.data.items) {
                const newCount = response.data.items.reduce(
                    (sum, item) => sum + (item.quantity || 1), 0
                );
                setCartCount(newCount);
            }

            return true;
        } catch (error) {
            console.error('❌ Error removing from cart:', error);
            return false;
        }
    };

    // Zaktualizuj ilość - POPRAWIONE
    const updateQuantity = async (productId, quantity) => {
        try {
            const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
            if (!token) {
                console.error('❌ No token for updateQuantity');
                return false;
            }

            // UWAGA: Backend używa @RequestParam, więc musimy użyć query params w URL
            const url = `http://localhost:8080/api/cart/update?productId=${productId}&quantity=${quantity}`;
            console.log(`✏️ Updating product ${productId} quantity to ${quantity}`);

            const response = await axios.put(
                url,
                {}, // pusty body
                {
                    headers: getAuthHeaders()
                }
            );

            setCart(response.data);

            // Aktualizuj licznik
            if (response.data.totalItems !== undefined) {
                setCartCount(response.data.totalItems);
            } else if (response.data.items) {
                const newCount = response.data.items.reduce(
                    (sum, item) => sum + (item.quantity || 1), 0
                );
                setCartCount(newCount);
            }

            return true;
        } catch (error) {
            console.error('❌ Error updating cart:', error);
            return false;
        }
    };

    // Wyczyść koszyk - POPRAWIONE
    const clearCart = async () => {
        try {
            const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
            if (!token) {
                console.error('❌ No token for clearCart');
                return false;
            }

            console.log('🗑️ Clearing cart...');
            await axios.delete('http://localhost:8080/api/cart/clear', {
                headers: getAuthHeaders()
            });
            setCart({ items: [] });
            setCartCount(0);
            localStorage.removeItem('cart_cache');
            return true;
        } catch (error) {
            console.error('❌ Error clearing cart:', error);
            return false;
        }
    };

    // Funkcja do czyszczenia cache
    const resetCartCache = () => {
        localStorage.removeItem('cart_cache');
        setCart({ items: [] });
        setCartCount(0);
        console.log('🧹 Cart cache cleared');
    };

    // ================== USEFFECTY ==================

    // 1. Zapisz cart do localStorage przy każdej zmianie
    useEffect(() => {
        console.log('💾 Cart changed, saving to cache...');
        if (cart && cart.items) {
            localStorage.setItem('cart_cache', JSON.stringify(cart));
            console.log('💾 Cart saved to localStorage');
        }
    }, [cart]);

    // 2. Przy starcie, wczytaj z localStorage
    useEffect(() => {
        console.log('🚀 CartProvider initializing...');
        const cachedCart = localStorage.getItem('cart_cache');
        const token = localStorage.getItem('token') || localStorage.getItem('accessToken');

        console.log('🚀 Has cached cart?', !!cachedCart);
        console.log('🚀 Has token?', !!token);

        if (cachedCart && token) {
            console.log('💾 Loading cached cart from localStorage');
            try {
                const parsedCart = JSON.parse(cachedCart);
                setCart(parsedCart);

                // Oblicz licznik z cache
                if (parsedCart.items) {
                    const count = parsedCart.items.reduce((sum, item) =>
                        sum + (item.quantity || 1), 0
                    );
                    setCartCount(count);
                    console.log('💾 Cart count from cache:', count);
                }
            } catch (error) {
                console.error('❌ Error loading cached cart:', error);
            }
        }

        // Jeśli zalogowany, pobierz świeże dane z API
        if (token && isAuthenticated) {
            console.log('🚀 User authenticated, fetching fresh cart data');
            fetchCart();
        } else {
            setLoading(false);
        }
    }, []); // Tylko przy mouncie

    // 3. Obserwuj zmianę autentykacji
    useEffect(() => {
        console.log('🔑 Auth changed:', isAuthenticated);
        if (isAuthenticated) {
            console.log('🔑 User authenticated, refreshing cart');
            fetchCart();
        } else {
            console.log('🔑 User not authenticated, clearing cart');
            setCart({ items: [] });
            setCartCount(0);
            setLoading(false);
        }
    }, [isAuthenticated, fetchCart]);

    // 4. Debugowanie
    useEffect(() => {
        console.log('🔍 CART STATE UPDATE:');
        console.log('- cart items count:', cart?.items?.length || 0);
        console.log('- cartCount:', cartCount);
        console.log('- loading:', loading);
        console.log('- isAuthenticated:', isAuthenticated);
    }, [cart, cartCount, loading, isAuthenticated]);

    // 5. Dodaj funkcję debugowania do window
    useEffect(() => {
        window.debugCart = async () => {
            console.log('=== DEBUG CART ===');
            const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
            console.log('Token exists:', !!token);
            console.log('Token (first 50 chars):', token?.substring(0, 50));
            console.log('Is authenticated:', isAuthenticated);

            // Test bezpośrednio API
            if (token) {
                try {
                    console.log('🔬 Testing API with fetch...');
                    const response = await fetch(
                        'http://localhost:8080/api/cart/add?productId=1&quantity=1',
                        {
                            method: 'POST',
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'application/json'
                            },
                            body: '{}'
                        }
                    );
                    console.log('🔬 Fetch status:', response.status);
                    console.log('🔬 Fetch ok:', response.ok);

                    if (response.ok) {
                        const data = await response.json();
                        console.log('🔬 Fetch data:', data);
                    } else {
                        console.log('🔬 Fetch failed:', response.statusText);
                    }
                } catch (error) {
                    console.error('🔬 Fetch error:', error);
                }
            }

            console.log('Current cart:', cart);
            console.log('Cart count:', cartCount);
        };

        console.log('🛠️ Debug function added: window.debugCart()');
    }, [cart, cartCount, isAuthenticated]);

    const value = {
        cart,
        cartCount,
        loading,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        refreshCart: fetchCart,
        resetCartCache
    };

    return (
        <CartContext.Provider value={value}>
            {children}
        </CartContext.Provider>
    );
};