param([string]$DocumentPath = 'docs/APF1_DesarrolloWebIntegrado_Casas_Anderson.docx')
$ErrorActionPreference='Stop'
$reportPath=(Resolve-Path -LiteralPath $DocumentPath).Path
$qaPath=Join-Path (Get-Location) 'artifacts/qa/final'
New-Item -ItemType Directory -Force -Path $qaPath | Out-Null
$word = New-Object -ComObject Word.Application
try {
    $word.Visible=$false
    $word.DisplayAlerts=0
    $document=$word.Documents.Open($reportPath)
    $document.Fields.Update() | Out-Null
    foreach($toc in $document.TablesOfContents) { $toc.Update() }
    $document.Repaginate()
    foreach($toc in $document.TablesOfContents) { $toc.UpdatePageNumbers() }
    $document.Save()
    $document.ExportAsFixedFormat((Join-Path $qaPath 'report.pdf'),17)
    Write-Output "Word $($word.Version); páginas: $($document.ComputeStatistics(2)); índice actualizado."
    $document.Close(0)
} finally { $word.Quit() }
