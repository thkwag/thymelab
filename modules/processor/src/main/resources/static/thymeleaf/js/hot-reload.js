(() => {
    let stompClient = null;
    let retryCount = 0;
    const maxRetries = 5;
    let dotCount = 0;
    let retryInterval = null;

    function reloadPage(event) {
        event.preventDefault();
        location.reload(true);
    }

    async function loadDependencies() {
        try {
            await loadScript('/thymeleaf/js/lib/sockjs.min.js');
            await loadScript('/thymeleaf/js/lib/stomp.min.js');
            connect();
        } catch (e) {
            console.error('[hot-reload] failed to load dependencies:', e);
        }
    }

    function loadScript(src) {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = src;
            script.onload = resolve;
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }

    function connect() {
        console.log('[hot-reload] connecting...');
        const socket = new SockJS('/hot-reload');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({},
            () => {
                console.log('[hot-reload] connected');
                removeStatusBar();
                retryCount = 0;
                clearInterval(retryInterval);
                retryInterval = null;

                stompClient.subscribe('/topic/reload', () => {
                    window.location.reload();
                });
            },
            error => {
                console.error('[hot-reload] error:', error);
                handleDisconnect();
            }
        );
    }

    function handleDisconnect() {
        if (stompClient !== null) {
            try {
                stompClient.disconnect();
            } catch (e) {
                console.error('[hot-reload] disconnect error:', e);
            }
            stompClient = null;
        }

        if (retryCount < maxRetries) {
            retryCount++;
            dotCount = 0;
            console.log(`[hot-reload] retrying... (${retryCount}/${maxRetries})`);
            updateRetryingMessage();
            if (!retryInterval) {
                retryInterval = setInterval(updateRetryingMessage, 500);
            }
            setTimeout(connect, 2000);
        } else {
            clearInterval(retryInterval);
            retryInterval = null;
            const refreshLink = document.createElement('a');
            refreshLink.href = '#';
            refreshLink.textContent = 'Click here to refresh';
            refreshLink.style.color = 'yellow';
            refreshLink.style.textDecoration = 'underline';
            refreshLink.style.cursor = 'pointer';
            refreshLink.style.userSelect = 'none';
            refreshLink.addEventListener('click', reloadPage);
            
            const message = document.createElement('div');
            message.textContent = 'Connection lost. ';
            message.appendChild(refreshLink);
            
            showStatusBar(message);
        }
    }

    function updateRetryingMessage() {
        dotCount = (dotCount + 1) % 4;
        const dots = '.'.repeat(dotCount);
        showStatusBar(`Connection lost. Retrying${dots}`);
    }

    function showStatusBar(content) {
        let statusBar = document.getElementById('connection-status-bar');
        if (!statusBar) {
            statusBar = document.createElement('div');
            statusBar.id = 'connection-status-bar';
            statusBar.style.position = 'fixed';
            statusBar.style.bottom = '0';
            statusBar.style.left = '0';
            statusBar.style.width = '100%';
            statusBar.style.backgroundColor = 'red';
            statusBar.style.color = 'white';
            statusBar.style.textAlign = 'left';
            statusBar.style.padding = '2px';
            statusBar.style.paddingLeft = '20px';
            statusBar.style.zIndex = '999999';
            document.body.appendChild(statusBar);
        }
        
        if (typeof content === 'string') {
            statusBar.textContent = content;
        } else {
            statusBar.innerHTML = '';
            statusBar.appendChild(content);
        }
    }

    function removeStatusBar() {
        const statusBar = document.getElementById('connection-status-bar');
        if (statusBar) {
            document.body.removeChild(statusBar);
        }
    }

    window.addEventListener('load', loadDependencies);
    window.addEventListener('beforeunload', () => {
        if (stompClient !== null) {
            stompClient.disconnect();
        }
        if (retryInterval) {
            clearInterval(retryInterval);
        }
    });
})(); 