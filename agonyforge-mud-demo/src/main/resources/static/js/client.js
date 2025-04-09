let socket = null;
let stompClient = null;

let uploadedFileMudType = null;

const scrollBackLength = 1000;

const commandHistory = [];
const commandHistoryMax = 50;
let commandHistoryIndex = -1;

let reconnectDelay = 2;
let isReconnecting = false;

const faviconDefault = '/img/favicon.ico';
const faviconNotify = '/img/favicon-green.ico';
let ownMessage = false;

// https://www.iana.org/assignments/websocket/websocket.xhtml
const clientWsErrorCodes = [1008, 2000]; // we should ask user to refresh

$(document).ready(function() {
    $("form").on("submit", function(event) {
        sendInput();
        return false;
    });

    connect();
});

$(document).on("keydown", function(event) {
    if (event.which === 9) { // don't tab away from input
        return false;
    }
});

$(document).on("keyup", function(event) {
    const inputBox = $("form input");

    if (event.which === 38) { // up arrow - command history prev
        commandHistoryIndex++;

        if (commandHistoryIndex >= commandHistory.length) {
            commandHistoryIndex = commandHistory.length - 1;
        }

        if (commandHistoryIndex >= 0) {
            inputBox.val(commandHistory[commandHistoryIndex]);
        }

        return false;
    } else if (event.which === 40) { // down arrow - command history next
        commandHistoryIndex--;

        if (commandHistoryIndex < 0) {
            commandHistoryIndex = -1;
        }

        if (commandHistoryIndex >= 0) {
            inputBox.val(commandHistory[commandHistoryIndex]);
        } else {
            inputBox.val("");
        }

        return false;
    }
});



function connect() {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    socket = new SockJS('/mud');
    stompClient = webstomp.over(socket, { heartbeat: false, protocols: ['v12.stomp']});
    stompClient.connect(
        {
            [header]: token,
        },
        function(frame) { // connectCallback
            console.log('Connected: ' + frame);
            showOutput(["[green]Connected to server."]);
            reconnectDelay = 2;

            stompClient.subscribe('/user/queue/output', function(message) {
                let msg = JSON.parse(message.body);
                showOutput(msg.output);

                if (ownMessage) {
                    ownMessage = false;
                } else {
                    setFavicon(faviconNotify);
                }
            },
            {});

            // reload of site after ban, kick, slay and import
            stompClient.subscribe('/user/queue/reload', function (message) {
                if (message.body === "reload") {
                    window.location.reload();
                }
                });

            // triggers a file chooser
            stompClient.subscribe('/user/queue/upload', function (message) {
                uploadedFileMudType = message.body;
                document.getElementById("fileInput").click();
            });

            // triggers a file download (character, item or map)
            stompClient.subscribe('/user/queue/download', function (message) {
                if (message.body === "download") {
                    window.location.href = "/download"; // TODO fix
                }
            });

            // triggers when file is uploaded
            $("#fileInput").on("change", function(event) {
                const file = event.target.files[0];
                if (!file) return;

                const reader = new FileReader();
                reader.onload = function(evt) {
                    const base64 = evt.target.result.split(",")[1];

                    const uploadMessage = {
                        filename: file.name,
                        contentType: file.type || "application/octet-stream",
                        base64Content: base64,
                        type: uploadedFileMudType
                    };

                    stompClient.send("/queue/import", {}, JSON.stringify(uploadMessage));
                };
            });


        },
        function(event) { // errorCallback
            console.log(`Connection error: ${event.code} => ${event.reason}`);

            if (clientWsErrorCodes.includes(event.code)) {
                if (event.reason) {
                    showOutput([`[red]Disconnected from server: ${event.reason}`]);
                } else {
                    showOutput(['[red]Disconnected from server.']);
                }
                showOutput(['[red]Please refresh your browser to log in again.']);
                return;
            } else {
                if (event.reason) {
                    showOutput([`[red]Encountered an error: ${event.reason}`]);
                } else {
                    showOutput(['[red]Disconnected from server.']);
                }
            }

            const delay = Math.random() * reconnectDelay;

            if (!isReconnecting) {
                showOutput([`[dred]Waiting for ${delay.toFixed(0)} seconds.`]);

                isReconnecting = true;

                setTimeout(function() {
                    showOutput(["[dyellow]Reconnecting to server..."]);

                    isReconnecting = false;

                    this.connect();
                }, delay * 1000);
            }

            reconnectDelay = Math.min(reconnectDelay * 2, 120);
        }
    );
}

function sendInput() {
    const inputBox = $("form input");

    commandHistoryIndex = -1;
    commandHistory.unshift(inputBox.val());

    if (commandHistory.length > commandHistoryMax) {
        commandHistory.pop();
    }

    $("#output-list").find("li:last-child").append("<span class='yellow'>" + htmlEscape(inputBox.val()) + "</span>");

    stompClient.send("/app/input", JSON.stringify({'input': inputBox.val()}));
    inputBox.val('');

    setFavicon(faviconDefault);
    ownMessage = true;
}

function showOutput(message) {
    const outputBox = $("#output-box");
    const outputList = $("#output-list");

    for (let i = 0; i < message.length; i++) {
        if ("" === message[i]) {
            outputList.append("<li>&nbsp;</li>");
        } else {
            outputList.append("<li><span>" + replaceColors(message[i]) + "</span></li>");
        }
    }

    // scroll to bottom
    outputBox.prop("scrollTop", outputBox.prop("scrollHeight"));

    // cut off the top output when it gets too big
    const scrollBackOverflow = outputList.find("li").length - scrollBackLength;

    if (scrollBackOverflow > 0) {
        outputList.find("li").slice(0, scrollBackOverflow).remove();
    }

}

const colors = [
    new RegExp('\\[(default)]', 'g'),
    new RegExp('\\[(black)]', 'g'),
    new RegExp('\\[(dblack)]', 'g'),
    new RegExp('\\[(white)]', 'g'),
    new RegExp('\\[(dwhite)]', 'g'),
    new RegExp('\\[(red)]', 'g'),
    new RegExp('\\[(dred)]', 'g'),
    new RegExp('\\[(yellow)]', 'g'),
    new RegExp('\\[(dyellow)]', 'g'),
    new RegExp('\\[(green)]', 'g'),
    new RegExp('\\[(dgreen)]', 'g'),
    new RegExp('\\[(cyan)]', 'g'),
    new RegExp('\\[(dcyan)]', 'g'),
    new RegExp('\\[(blue)]', 'g'),
    new RegExp('\\[(dblue)]', 'g'),
    new RegExp('\\[(magenta)]', 'g'),
    new RegExp('\\[(dmagenta)]', 'g')
];

function replaceColors(message) {
    colors.forEach(color => message = message.replace(color, `<span class='$1'>`));
    return message;
}

function htmlEscape(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\//g, '&#x2F;');
}

function setFavicon(filename) {
    let link = $("link[rel='icon']");

    if (!link) {
        link = $("head").append(`<link rel='icon' href='${filename}'`);
    }

    link.attr("href", filename);
}
