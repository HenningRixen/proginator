document.addEventListener("DOMContentLoaded", function () {
    const revealBtn = document.getElementById("show-solutions-btn");
    const solutionsContainer = document.getElementById("exam-solutions-container");

    if (revealBtn && solutionsContainer) {
        revealBtn.addEventListener("click", function () {
            solutionsContainer.hidden = false;
            revealBtn.disabled = true;
            revealBtn.textContent = "Loesungen sind sichtbar";
        });
    }

    const completeButtons = document.querySelectorAll(".exam-complete-btn");
    completeButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const exerciseId = this.getAttribute("data-exercise-id");
            const status = this.parentElement.querySelector(".exam-complete-status");
            const originalText = this.textContent;

            this.disabled = true;
            this.textContent = "Speichere...";

            const csrfToken = document.querySelector('meta[name=\"_csrf\"]')?.getAttribute("content") || "";
            const csrfHeader = document.querySelector('meta[name=\"_csrf_header\"]')?.getAttribute("content") || "X-CSRF-TOKEN";

            fetch("/exam/" + exerciseId + "/complete", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                }
            })
                .then(function (response) {
                    return response.json();
                })
                .then(function (data) {
                    if (data.status === "success") {
                        button.textContent = "Erledigt";
                        if (status) {
                            status.textContent = "Erledigt";
                            status.classList.add("done");
                        }
                        return;
                    }

                    button.disabled = false;
                    button.textContent = originalText;
                    if (status && data.message) {
                        status.textContent = data.message;
                    }
                })
                .catch(function () {
                    button.disabled = false;
                    button.textContent = originalText;
                    if (status) {
                        status.textContent = "Fehler beim Speichern";
                    }
                });
        });
    });
});
