# Verificación del informe

- Documento: `docs/APF1_DesarrolloWebIntegrado_Casas_Anderson.docx`.
- Fecha de revisión: 5 de septiembre de 2026.
- El renderizador `render_docx.py` no pudo convertir: no había `soffice.exe` disponible.
- Alternativa ejecutada: Microsoft Word 16.0 mediante `scripts/render_report.ps1`.
- Se actualizaron campos, índice y números de página, se guardó el DOCX y se exportó un PDF para revisión interna.
- Poppler convirtió el PDF en 21 imágenes PNG. Se inspeccionaron las 21 páginas, incluidas tablas, capturas e índice.
- Se corrigieron un borde de título, la autorreferencia del índice y los saltos de página de móvil, Lean Canvas y cronograma.
- Resultado final: 21 páginas, sin tablas cortadas, superposiciones ni párrafos aislados por desbordamiento.
- Los PNG/PDF de revisión permanecen en `artifacts/qa/` y no forman parte del entregable solicitado.
- Las capturas de aplicación conservadas en `artifacts/evidence/screenshots/` proceden del navegador local; no son prototipos dibujados.
