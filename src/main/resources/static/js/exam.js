document.addEventListener("DOMContentLoaded", function () {
    if (window.hljs) {
        window.hljs.highlightAll();
    }

    const revealBtn = document.getElementById("show-solutions-btn");
    const solutionsContainer = document.getElementById("exam-solutions-container");
    const progressText = document.getElementById("solution-progress-text");
    const gateHint = document.getElementById("solution-gate-hint");

    if (revealBtn && solutionsContainer) {
        solutionsContainer.hidden = true;
        solutionsContainer.classList.remove("is-visible");
        solutionsContainer.setAttribute("aria-hidden", "true");

        revealBtn.addEventListener("click", function () {
            solutionsContainer.hidden = false;
            solutionsContainer.classList.add("is-visible");
            solutionsContainer.setAttribute("aria-hidden", "false");
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

                        if (revealBtn && progressText) {
                            const completedCount = Number(data.completedCount ?? revealBtn.dataset.completedCount ?? 0);
                            const totalCount = Number(data.totalCount ?? revealBtn.dataset.totalCount ?? 3);
                            revealBtn.dataset.completedCount = String(completedCount);
                            revealBtn.dataset.totalCount = String(totalCount);
                            progressText.textContent = completedCount + " von " + totalCount + " Aufgaben erledigt";

                            const canReveal = data.canRevealSolutions === true || completedCount >= totalCount;
                            if (canReveal) {
                                revealBtn.disabled = false;
                                if (gateHint) {
                                    gateHint.textContent = "Alle Aufgaben erledigt. Du kannst jetzt die Loesungen anzeigen.";
                                }
                            }
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
