# DESIGN SYSTEM: INDUSTRIAL BRUTALISM

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Sonic Altar."** This isn't just a music interface; it is a digital monument to the raw power of deathcore and metal culture. We are moving away from the safe, sterile layouts of modern tech and embracing an aesthetic of **Aggressive Precision**. 

The system breaks the standard "template" through intentional asymmetry—utilizing high-contrast typography and overlapping elements that mimic the chaotic yet calculated structure of a breakdown. We utilize the zero-radius corner scale to enforce a harsh, uncompromising silhouette. The experience should feel like distressed metal: cold, heavy, and visceral.

---

## 2. Colors
Our palette is rooted in the depth of a mosh pit at midnight. It balances total darkness with the violent energy of crimson.

*   **Primary (#ffb4a8 / #8b0000):** The 'Bloody Crimson.' Used for high-impact actions and brand signifiers. Use `primary_container` (#8b0000) for deep, textural backgrounds and `primary` for sharp, high-contrast accents.
*   **Surface Hierarchy (The Void):**
    *   **Background (#131313):** The base pitch black.
    *   **Surface-Container-Lowest (#0e0e0e):** Use for recessed areas to create a sense of infinite depth.
    *   **Surface-Container-Highest (#353534):** The 'Charcoal Grey.' Reserved for foreground elements that need to feel "closer" to the user.
*   **The "No-Line" Rule:** 1px solid borders are strictly prohibited for sectioning. Boundaries must be defined through background shifts. For example, a track list sitting on `surface` should be contained within a `surface-container-low` block.
*   **Signature Textures:** Apply a "distressed metal" noise or scratch overlay (mimicking the brand logo) at 5-8% opacity over `surface-container` tiers. 
*   **Glass & Gradient:** Use semi-transparent `primary_container` with a `backdrop-blur` for floating player controls to maintain the "gritty" atmosphere while ensuring legibility.

---

## 3. Typography
The typography is a duel between industrial utility and aggressive expression.

*   **Display (Space Grotesk):** Massive, all-caps, and unapologetic. Use `display-lg` (3.5rem) for hero headlines. Tighten letter-spacing (-0.05em) to increase the "heavy" feel.
*   **Headlines & Titles (Space Grotesk / Work Sans):** Use `headline-lg` for section headers. These should feel like propaganda posters—bold and high-contrast.
*   **Body (Work Sans):** Functional and legible. Even in a dark environment, the `on_surface` (#e5e2e1) provides the necessary contrast against the charcoal backgrounds.
*   **Hierarchy Note:** Use all-caps for labels and titles to mirror the "TRACKGOD" logo's intensity. Only use sentence case for long-form body copy.

---

## 4. Elevation & Depth
In this system, we do not "lift" objects with light; we carve them out of the dark.

*   **The Layering Principle:** Depth is achieved by stacking. A card (Surface-Container-High) sits on a section (Surface-Container-Low). This creates a "machined" look where parts feel assembled rather than floating.
*   **Ambient Shadows:** Traditional drop shadows are replaced by "Glow Bleed." When a primary element (like a CTA) needs to stand out, use a large, 20px-40px blurred shadow using a 10% opacity version of `on_primary_container` (#ff907f) to simulate a red neon glow.
*   **The "Ghost Border" Fallback:** If a container needs more definition, use `outline_variant` (#5a403c) at 15% opacity. It should look like a faint scratch on metal, not a UI border.
*   **Roundedness:** All elements are `0px` radius. Sharp edges are mandatory to maintain the aggressive visual language.

---

## 5. Components

### Buttons
*   **Primary:** Solid `primary_container` (#8b0000) with `on_primary` (#690000) text. Sharp corners. Use a subtle inner-glow of `primary` to simulate a "beveled metal" edge.
*   **Secondary:** Ghost style. No background, `outline` (#aa8984) text, and a `primary` under-glow on hover.
*   **States:** On press, the button should shift to `primary_fixed_dim` to simulate a physical mechanical depress.

### Input Fields
*   **Styling:** Background of `surface_container_lowest`. No bottom border. Instead, use a 2px vertical "accent bar" of `primary` on the left side of the input when focused.
*   **Text:** `on_surface_variant` for placeholders, shifting to `primary` for active input.

### Cards & Lists
*   **No Dividers:** Lists are separated by `spacing-4` (0.9rem) or a subtle shift from `surface-container-low` to `surface-container-high`.
*   **Interactions:** Hovering over a list item (e.g., a song) should trigger a `surface_bright` background shift and a red "scratch" texture overlay.

### Media Player & Sliders
*   **Track:** `surface_container_highest`.
*   **Progress:** `primary` (#ffb4a8).
*   **Knobs:** Square `0px` handles. Use the "TRACKGOD" logo as a custom scrub icon for high-end branded touchpoints.

---

## 6. Do's and Don'ts

### Do
*   **Do** embrace negative space. The pitch-black `background` (#131313) is your primary canvas; let it breathe to emphasize the red accents.
*   **Do** use extreme typographic scales. A tiny `label-sm` next to a massive `display-lg` creates the tension required for this genre.
*   **Do** treat the logo as the "Master Element." If a layout feels empty, a large, cropped, low-opacity (5%) logo watermark in the background adds instant texture.

### Don't
*   **Don't** use rounded corners. Even a 2px radius destroys the "deathcore" energy.
*   **Don't** use standard blue/green for success/info states. Interpret all feedback through the red (`primary`) and charcoal (`secondary`) spectrum. Use `error` (#ffb4ab) only for critical system failures.
*   **Don't** use soft transitions. Interactions should be fast, snappy, and "mechanical" (e.g., 150ms Linear or Ease-In).