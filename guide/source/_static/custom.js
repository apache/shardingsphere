document.addEventListener("DOMContentLoaded", function() {
  const captions = document.querySelectorAll(".wy-menu-vertical .caption-text");
  captions.forEach(function(caption) {
    if (caption.textContent.trim() === "Содержание:") {
      const link = document.createElement("a");
      link.href = document.querySelector("a.docs__item")?.getAttribute("href") || "index.html";
      link.textContent = caption.textContent;
      caption.textContent = "";
      caption.appendChild(link);
    }
  });
});
