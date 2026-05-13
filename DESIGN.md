# Netflix Redesign: The Cinematic Immersion

## 1. Overview & Creative North Star
**Creative North Star: "The Cinematic Portal"**
This design system is engineered to elevate the Netflix experience beyond a simple grid of posters. It aims to create a "Cinematic Portal" that feels alive, responsive, and deeply immersive. We move away from flat lists to a layered, dynamic environment where content is the hero.

The design breaks the standard "web app" look by utilizing:
*   **Dynamic Backgrounds:** The background of the entire app subtly shifts to match the color palette of the currently focused movie/show poster (a soft, blurred ambient glow).
*   **Glassmorphism & Depth:** Navigation bars and detail overlays use a high-end "Frosted Black" effect, creating a sense of depth and physical layering.
*   **Micro-Animations:** Every selection, scroll, and focus event is accompanied by fluid, spring-based animations.

## 2. Colors & Surface Philosophy
The palette is rooted in absolute darkness to make content "pop," with high-vibrancy accents.

### The "No-Line" Rule
**Explicit Instruction:** Designers are strictly prohibited from using 1px solid borders for sectioning. Boundaries must be defined solely through background tonal shifts or glassmorphism.
*   Use `surface` (#000000) for the base.
*   Use `surface_container` (#141414) for cards and focused states.
*   Use `surface_container_high` (#1a1a1a) for active navigation elements.

### The "Signature Red" Rule
*   **Primary Accent:** #E50914 (Netflix Red). Used only for the logo, primary call-to-action buttons (Play), and active progress bars.
*   **Glow Effect:** Primary buttons should have a soft, 10px blurred red outer glow (`box-shadow: 0 0 20px rgba(229, 9, 20, 0.4)`).

## 3. Typography: Bold Authority
We use a high-impact typographic scale to guide the user through thousands of titles.

*   **Display & Headlines (Inter):** Use `display-lg` for movie titles. Weights must be Extra Bold or Black (800-900) to command attention.
*   **Body & Metadata (Inter):** Use `body-md` for descriptions and `label-sm` for metadata (Year, Rating, HD). Metadata should use a slightly higher letter-spacing (+0.05em) for a "spec-sheet" look.

## 4. Elevation & Depth
Depth is achieved through **Glassmorphism** and **Tonal Layering**.

*   **The Glass Header:** The top navigation bar must use `surface` at 60% opacity with a `50px` backdrop-blur. This allows the movie posters to bleed through as the user scrolls.
*   **Focused Cards:** When a movie card is focused (hovered/selected), it must scale by 10% and gain a `surface_container_high` background with a subtle red "Ghost Border" (10% opacity Netflix Red).

## 5. Components

### Movie Cards (The Grid)
*   **Ratio:** 16:9 for Featured, 2:3 for standard posters.
*   **Radius:** `ROUND_FOUR` (4px). Netflix is precise; we avoid overly round corners to maintain a professional, cinematic feel.
*   **Focus State:** Scale 1.1x, soft drop shadow, and a 2px "Netflix Red" progress bar at the bottom if the title has been partially watched.

### Buttons (The Trigger)
*   **Primary (Play):** Background: #FFFFFF. Text: #000000. Weight: Bold. Icon: Play symbol.
*   **Secondary (More Info):** Background: rgba(109, 109, 110, 0.7). Text: #FFFFFF. Backdrop-blur: 10px.

### Profile Selection
*   **Icon Size:** Large squares (200px).
*   **Interactive:** On hover, the icon glows and the profile name turns white.

## 6. Do's and Don'ts

### Do
*   **DO** use extreme high-quality images.
*   **DO** use ambient blurs for background transitions.
*   **DO** keep the UI simple. Let the posters speak.

### Don't
*   **DON'T** use borders.
*   **DON'T** use standard blue for links.
*   **DON'T** use white backgrounds for any section. This is a dark-room experience.
