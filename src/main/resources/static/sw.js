/**
 * AutoRent Service Worker
 * Обеспечивает кэширование и offline поддержку
 */

const CACHE_NAME = 'autorent-v1.0.0';
const STATIC_CACHE = 'autorent-static-v1.0.0';
const DYNAMIC_CACHE = 'autorent-dynamic-v1.0.0';

// Критические ресурсы для кэширования
const STATIC_ASSETS = [
    '/',
    '/css/main.css',
    '/js/main.js',
    '/images/default-car.jpg',
    '/images/car-placeholder.jpg',
    '/favicon.ico',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'
];

// Страницы для кэширования
const PAGES_TO_CACHE = [
    '/auth/login',
    '/auth/register',
    '/cars/available',
    '/rentals',
    '/account/profile'
];

// Установка Service Worker
self.addEventListener('install', event => {
    console.log('Service Worker: Installing...');
    
    event.waitUntil(
        Promise.all([
            // Кэшируем статические ресурсы
            caches.open(STATIC_CACHE)
                .then(cache => {
                    console.log('Service Worker: Caching static assets');
                    return cache.addAll(STATIC_ASSETS);
                }),
            
            // Кэшируем страницы
            caches.open(DYNAMIC_CACHE)
                .then(cache => {
                    console.log('Service Worker: Caching pages');
                    return cache.addAll(PAGES_TO_CACHE);
                })
        ])
    );
    
    // Активируем новый Service Worker немедленно
    self.skipWaiting();
});

// Активация Service Worker
self.addEventListener('activate', event => {
    console.log('Service Worker: Activating...');
    
    event.waitUntil(
        caches.keys()
            .then(cacheNames => {
                return Promise.all(
                    cacheNames.map(cacheName => {
                        // Удаляем старые кэши
                        if (cacheName !== STATIC_CACHE && cacheName !== DYNAMIC_CACHE) {
                            console.log('Service Worker: Deleting old cache', cacheName);
                            return caches.delete(cacheName);
                        }
                    })
                );
            })
    );
    
    // Берем контроль над всеми клиентами
    self.clients.claim();
});

// Перехват запросов
self.addEventListener('fetch', event => {
    const { request } = event;
    const url = new URL(request.url);
    
    // Пропускаем не-GET запросы
    if (request.method !== 'GET') {
        return;
    }
    
    // Пропускаем запросы к API
    if (url.pathname.startsWith('/api/')) {
        return;
    }
    
    // Пропускаем запросы к внешним ресурсам (кроме CDN)
    if (!url.origin.includes(self.location.origin) && 
        !url.origin.includes('cdn.jsdelivr.net') && 
        !url.origin.includes('cdnjs.cloudflare.com')) {
        return;
    }
    
    event.respondWith(
        caches.match(request)
            .then(response => {
                // Если ресурс найден в кэше
                if (response) {
                    console.log('Service Worker: Serving from cache', request.url);
                    return response;
                }
                
                // Если ресурс не найден в кэше, загружаем из сети
                return fetch(request)
                    .then(response => {
                        // Проверяем, что ответ валидный
                        if (!response || response.status !== 200 || response.type !== 'basic') {
                            return response;
                        }
                        
                        // Клонируем ответ для кэширования
                        const responseToCache = response.clone();
                        
                        // Определяем, в какой кэш сохранить
                        let cacheName = DYNAMIC_CACHE;
                        if (STATIC_ASSETS.includes(request.url) || 
                            request.url.includes('.css') || 
                            request.url.includes('.js') || 
                            request.url.includes('.png') || 
                            request.url.includes('.jpg') || 
                            request.url.includes('.ico')) {
                            cacheName = STATIC_CACHE;
                        }
                        
                        // Кэшируем ответ
                        caches.open(cacheName)
                            .then(cache => {
                                console.log('Service Worker: Caching new resource', request.url);
                                cache.put(request, responseToCache);
                            });
                        
                        return response;
                    })
                    .catch(error => {
                        console.log('Service Worker: Fetch failed', request.url, error);
                        
                        // Для HTML страниц возвращаем offline страницу
                        if (request.headers.get('accept').includes('text/html')) {
                            return caches.match('/offline.html');
                        }
                        
                        // Для других ресурсов возвращаем fallback
                        if (request.url.includes('.css')) {
                            return caches.match('/css/main.css');
                        }
                        
                        if (request.url.includes('.js')) {
                            return caches.match('/js/main.js');
                        }
                        
                        if (request.url.includes('.png') || request.url.includes('.jpg')) {
                            return caches.match('/images/default-car.jpg');
                        }
                    });
            })
    );
});

// Обработка push уведомлений
self.addEventListener('push', event => {
    console.log('Service Worker: Push received');
    
    const options = {
        body: event.data ? event.data.text() : 'Новое уведомление от AutoRent',
        icon: '/images/icon-192x192.png',
        badge: '/images/badge-72x72.png',
        vibrate: [100, 50, 100],
        data: {
            dateOfArrival: Date.now(),
            primaryKey: 1
        },
        actions: [
            {
                action: 'explore',
                title: 'Открыть приложение',
                icon: '/images/icon-96x96.png'
            },
            {
                action: 'close',
                title: 'Закрыть',
                icon: '/images/icon-96x96.png'
            }
        ]
    };
    
    event.waitUntil(
        self.registration.showNotification('AutoRent', options)
    );
});

// Обработка кликов по уведомлениям
self.addEventListener('notificationclick', event => {
    console.log('Service Worker: Notification click received');
    
    event.notification.close();
    
    if (event.action === 'explore') {
        event.waitUntil(
            clients.openWindow('/')
        );
    }
});

// Обработка синхронизации в фоне
self.addEventListener('sync', event => {
    console.log('Service Worker: Background sync', event.tag);
    
    if (event.tag === 'background-sync') {
        event.waitUntil(doBackgroundSync());
    }
});

// Функция фоновой синхронизации
async function doBackgroundSync() {
    try {
        // Здесь можно добавить логику синхронизации данных
        console.log('Service Worker: Background sync completed');
    } catch (error) {
        console.error('Service Worker: Background sync failed', error);
    }
}

// Обработка сообщений от клиента
self.addEventListener('message', event => {
    console.log('Service Worker: Message received', event.data);
    
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
    
    if (event.data && event.data.type === 'GET_VERSION') {
        event.ports[0].postMessage({ version: CACHE_NAME });
    }
}); 