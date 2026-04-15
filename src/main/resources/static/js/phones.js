(function () {
    var API_BASE = "/api/phones";
    var STORAGE_KEY = "phones.adminApiKey";

    document.addEventListener("DOMContentLoaded", function () {
        syncAdminKeyInputs();
        showStatusFromQuery();
        bindSearchBox();
        bindDeleteActions();
        bindManualQrForm();
        bindImagePreview();
        bindImageCompression();
        renderQrCode();
        initQrScanner();
    });

    function syncAdminKeyInputs() {
        var storedKey = sessionStorage.getItem(STORAGE_KEY) || "";
        var inputs = document.querySelectorAll("#adminApiKey");
        Array.prototype.forEach.call(inputs, function (input) {
            input.value = storedKey;
            input.addEventListener("input", function () {
                sessionStorage.setItem(STORAGE_KEY, input.value.trim());
            });
        });
    }

    function bindDeleteActions() {
        var buttons = document.querySelectorAll("button[onclick^='deleteProduct']");
        if (!buttons.length) {
            return;
        }

        Array.prototype.forEach.call(buttons, function (button) {
            button.addEventListener("click", function (event) {
                event.preventDefault();
                deleteProduct(button);
            });
        });
    }

    function bindManualQrForm() {
        var form = document.getElementById("manualQrForm");
        if (!form) {
            return;
        }

        form.addEventListener("submit", function (event) {
            event.preventDefault();
            var input = document.getElementById("manualQrValue");
            redirectFromQrValue(input.value.trim());
        });
    }

    function bindImagePreview() {
        var input = document.getElementById("imagem");
        if (!input) {
            return;
        }

        input.addEventListener("change", function () {
            if (!input.files || !input.files.length) {
                return;
            }

            var preview = document.getElementById("novaImagemPreview");
            if (!preview) {
                return;
            }

            preview.src = URL.createObjectURL(input.files[0]);
            preview.classList.remove("d-none");
        });
    }

    function bindImageCompression() {
        var input = document.getElementById("imagem");
        if (!input) {
            return;
        }

        input.addEventListener("change", function () {
            if (!input.files || !input.files.length) {
                return;
            }

            var file = input.files[0];
            if (!file.type || file.type.indexOf("image/") !== 0) {
                return;
            }

            var maxBytesField = document.getElementById("maxImagemBytes");
            var maxBytes = maxBytesField ? Number(maxBytesField.value) : 1048576;

            if (file.size <= maxBytes) {
                return;
            }

            compressImage(file, maxBytes)
                .then(function (compressedFile) {
                    replaceInputFile(input, compressedFile);
                    updatePreviewFromFile(compressedFile);
                    showFeedback("A imagem foi reduzida automaticamente antes do envio.", "info");
                })
                .catch(function () {
                    showFeedback("Não foi possível reduzir a imagem automaticamente. Escolha uma imagem menor.", "warning");
                });
        });
    }

    function renderQrCode() {
        var container = document.getElementById("qrCodeContainer");
        var debug = document.getElementById("qr-debug");
        if (debug) debug.textContent = "Container: " + (container ? "found" : "not found") + ", QRCode: " + typeof QRCode;
        
        if (!container || typeof QRCode === "undefined") {
            if (debug) debug.textContent += " - QRCode not rendered";
            return;
        }

        var qrCodeValue = container.getAttribute("data-qr-code");
        if (!qrCodeValue) {
            if (debug) debug.textContent += " - No QR code value";
            return;
        }

        new QRCode(container, {
            text: qrCodeValue,
            width: 220,
            height: 220
        });
        if (debug) debug.textContent += " - QRCode rendered!";
    }

    function compressImage(file, maxBytes) {
        return new Promise(function (resolve, reject) {
            var reader = new FileReader();
            reader.onload = function (event) {
                var image = new Image();
                image.onload = function () {
                    var canvas = document.createElement("canvas");
                    var dimensions = fitDimensions(image.width, image.height, 1600);
                    canvas.width = dimensions.width;
                    canvas.height = dimensions.height;

                    var context = canvas.getContext("2d");
                    context.drawImage(image, 0, 0, canvas.width, canvas.height);

                    var quality = 0.85;
                    exportCompressed(canvas, quality, maxBytes, resolve, reject);
                };
                image.onerror = reject;
                image.src = event.target.result;
            };
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }

    function exportCompressed(canvas, quality, maxBytes, resolve, reject) {
        canvas.toBlob(function (blob) {
            if (!blob) {
                reject(new Error("Falha ao gerar imagem comprimida"));
                return;
            }

            if (blob.size <= maxBytes || quality <= 0.45) {
                resolve(new File([blob], "phone.jpg", { type: "image/jpeg" }));
                return;
            }

            exportCompressed(canvas, quality - 0.1, maxBytes, resolve, reject);
        }, "image/jpeg", quality);
    }

    function fitDimensions(width, height, maxDimension) {
        if (width <= maxDimension && height <= maxDimension) {
            return { width: width, height: height };
        }

        if (width > height) {
            return {
                width: maxDimension,
                height: Math.round((height / width) * maxDimension)
            };
        }

        return {
            width: Math.round((width / height) * maxDimension),
            height: maxDimension
        };
    }

    function replaceInputFile(input, file) {
        var dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        input.files = dataTransfer.files;
    }

    function updatePreviewFromFile(file) {
        var preview = document.getElementById("novaImagemPreview");
        if (!preview) {
            return;
        }

        preview.src = URL.createObjectURL(file);
        preview.classList.remove("d-none");
    }

    function initQrScanner() {
        var scannerElement = document.getElementById("qr-reader");
        if (!scannerElement || typeof Html5Qrcode === "undefined") {
            return;
        }

        var html5QrCode = new Html5Qrcode("qr-reader");
        Html5Qrcode.getCameras()
            .then(function (devices) {
                if (!devices || !devices.length) {
                    showFeedback("Não foi encontrada nenhuma câmara neste dispositivo.", "warning");
                    return;
                }

                var preferredCameraId = devices[0].id;
                for (var i = 0; i < devices.length; i++) {
                    if (devices[i].label && devices[i].label.toLowerCase().indexOf("back") >= 0) {
                        preferredCameraId = devices[i].id;
                        break;
                    }
                }

                return html5QrCode.start(
                    preferredCameraId,
                    { fps: 10, qrbox: { width: 250, height: 250 } },
                    function (decodedText) {
                        html5QrCode.stop().finally(function () {
                            redirectFromQrValue(decodedText);
                        });
                    }
                );
            })
            .catch(function (error) {
                showFeedback("Não foi possível iniciar o scanner: " + error, "warning");
            });
    }

    function redirectFromQrValue(value) {
        if (!value) {
            showFeedback("Indique um código único ou um URL válido.", "warning");
            return;
        }

        if (isAbsoluteUrl(value)) {
            window.location.href = value;
            return;
        }

        window.location.href = "/phones/codigo/" + encodeURIComponent(extractCodigo(value));
    }

    function isAbsoluteUrl(value) {
        return /^https?:\/\//i.test(value);
    }

    function extractCodigo(value) {
        var trimmed = value.trim();
        var match = trimmed.match(/\/phones\/codigo\/([^/?#]+)/i);
        if (match && match[1]) {
            return decodeURIComponent(match[1]);
        }
        return trimmed;
    }

    function buildHeaders(withJson) {
        var adminKeyInput = document.getElementById("adminApiKey");
        var adminKey = adminKeyInput ? adminKeyInput.value.trim() : "";
        if (!adminKey) {
            return null;
        }

        sessionStorage.setItem(STORAGE_KEY, adminKey);

        var headers = {
            Authorization: "Bearer " + adminKey
        };

        if (withJson) {
            headers["Content-Type"] = "application/json";
        }

        return headers;
    }

    function extractError(response) {
        return response.text().then(function (body) {
            var message = body;
            try {
                var parsed = JSON.parse(body);
                message = parsed.message || parsed.error || body;
            } catch (error) {
                message = body || ("Pedido falhou com estado HTTP " + response.status + ".");
            }
            throw new Error(message);
        });
    }

    function showStatusFromQuery() {
        var params = new URLSearchParams(window.location.search);
        var status = params.get("status");

        if (status === "created") {
            showFeedback("phone criado com sucesso.", "success");
        } else if (status === "updated") {
            showFeedback("phone atualizado com sucesso.", "success");
        } else if (status === "deleted") {
            showFeedback("phone apagado com sucesso.", "success");
        }
    }

    function bindSearchBox() {
        var input = document.getElementById("phoneBusca");
        var btnBuscar = document.getElementById("btnphoneBuscar");
        var btnLimpar = document.getElementById("btnphoneLimpar");
        if (!input || !btnBuscar || !btnLimpar) {
            return;
        }

        btnBuscar.addEventListener("click", function () {
            filterProductsByName(input.value);
        });

        input.addEventListener("keyup", function (event) {
            if (event.key === "Enter") {
                filterProductsByName(input.value);
            }
        });

        btnLimpar.addEventListener("click", function () {
            input.value = "";
            filterProductsByName("");
        });
    }

    function filterProductsByName(query) {
        var term = (query || "").trim().toLowerCase();
        var rows = document.querySelectorAll("#phones-lista tbody tr");

        Array.prototype.forEach.call(rows, function (row) {
            var nameCell = row.querySelector("td:nth-child(2)");
            var categoryCell = row.querySelector("td:nth-child(6)");
            if (!nameCell || !categoryCell) {
                return;
            }
            var name = (nameCell.textContent || "").trim().toLowerCase();
            var category = (categoryCell.textContent || "").trim().toLowerCase();
            var show = !term || name.indexOf(term) >= 0 || category.indexOf(term) >= 0;
            row.style.display = show ? "" : "none";
        });
    }

    function showFeedback(message, type) {
        var container = document.getElementById("feedback");
        if (!container) {
            return;
        }

        container.innerHTML =
            '<div class="alert alert-' + type + ' alert-dismissible fade show" role="alert">' +
            message +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
            "</div>";
    }

    window.deleteProduct = function (buttonElement) {
        var row = buttonElement.closest("tr");
        var id = row.querySelector("td.ps-4").textContent.trim();
        var nome = row.querySelector("td:nth-child(2)").textContent || "este phone";

        if (!window.confirm('Apagar "' + nome + '"?')) {
            return;
        }

        var headers = buildHeaders(false);
        if (!headers) {
            showFeedback("Indique a chave de administrador para apagar registos.", "warning");
            return;
        }

        fetch(API_BASE + "/" + id, {
            method: "DELETE",
            headers: headers
        })
            .then(function (response) {
                if (!response.ok) {
                    return extractError(response);
                }

                window.location.href = "/phones?status=deleted";
            })
            .catch(function (error) {
                showFeedback(error.message, "danger");
            });
    };
}());
