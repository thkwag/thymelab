(() => {
    let stompClient = null;
    let retryCount = 0;
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
                showSuccessBar();
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

        dotCount = 0;
        updateRetryingMessage();
        
        if (!retryInterval) {
            retryInterval = setInterval(updateRetryingMessage, 500);
        }
        
        setTimeout(connect, 2000);
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
            statusBar.style.fontSize = '11px';
            statusBar.style.fontFamily = 'Consolas, monospace';
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

    function showSuccessBar() {
        let statusBar = document.getElementById('connection-status-bar');
        if (statusBar) {
            statusBar.style.backgroundColor = '#28a745';
            statusBar.textContent = 'Connected';
            setTimeout(removeStatusBar, 2000);
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