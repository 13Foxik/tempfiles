#!/usr/bin/env python3
"""
Script to add Приложение Б and В to the diploma document.
Uses only built-in Python libraries (zipfile, xml.etree.ElementTree).
"""

import zipfile
import xml.etree.ElementTree as ET
import os
import shutil
import tempfile

# Paths
DOCX_PATH = '/projects/sandbox/tempfiles/docs/СмешновДиплом.docx'

# Word XML namespace
W_NS = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
# Register namespaces to avoid ns0: prefixes
NAMESPACES = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships',
    'wp': 'http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing',
    'a': 'http://schemas.openxmlformats.org/drawingml/2006/main',
    'pic': 'http://schemas.openxmlformats.org/drawingml/2006/picture',
    'mc': 'http://schemas.openxmlformats.org/markup-compatibility/2006',
    'w14': 'http://schemas.microsoft.com/office/word/2010/wordml',
    'w15': 'http://schemas.microsoft.com/office/word/2012/wordml',
    'wps': 'http://schemas.microsoft.com/office/word/2010/wordprocessingShape',
    'wpg': 'http://schemas.microsoft.com/office/word/2010/wordprocessingGroup',
    'wpc': 'http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas',
    'cx': 'http://schemas.microsoft.com/office/drawing/2014/chartex',
    'cx1': 'http://schemas.microsoft.com/office/drawing/2015/9/8/chartex',
    'cx2': 'http://schemas.microsoft.com/office/drawing/2015/10/21/chartex',
    'cx3': 'http://schemas.microsoft.com/office/drawing/2016/5/9/chartex',
    'cx4': 'http://schemas.microsoft.com/office/drawing/2016/5/10/chartex',
    'cx5': 'http://schemas.microsoft.com/office/drawing/2016/5/11/chartex',
    'cx6': 'http://schemas.microsoft.com/office/drawing/2016/5/12/chartex',
    'cx7': 'http://schemas.microsoft.com/office/drawing/2016/5/13/chartex',
    'cx8': 'http://schemas.microsoft.com/office/drawing/2016/5/14/chartex',
    'm': 'http://schemas.openxmlformats.org/officeDocument/2006/math',
    'o': 'urn:schemas-microsoft-com:office:office',
    'v': 'urn:schemas-microsoft-com:vml',
    'wne': 'http://schemas.microsoft.com/office/word/2006/wordml',
    'w10': 'urn:schemas-microsoft-com:office:word',
}

# Source code files
DISH_CS = '/projects/sandbox/EatTogether/EatTogether.MAUI/Models/Dish.cs'
PLATE_CS = '/projects/sandbox/EatTogether/EatTogether.MAUI/Models/Plate.cs'
FAMILY_CS = '/projects/sandbox/EatTogether/EatTogether.MAUI/Models/Family.cs'
MENU_VM_CS = '/projects/sandbox/EatTogether/EatTogether.MAUI/ViewModels/MenuViewModel.cs'
PLATE_VM_CS = '/projects/sandbox/EatTogether/EatTogether.MAUI/ViewModels/PlateViewModel.cs'


def read_source_file(path):
    """Read a source code file and return its content."""
    with open(path, 'r', encoding='utf-8-sig') as f:
        return f.read()


def make_paragraph(text, bold=False, font_size=None, font_name=None, centered=False):
    """Create a Word XML paragraph element with given text and formatting."""
    p = ET.SubElement(ET.Element('dummy'), f'{{{W_NS}}}p')
    
    # Paragraph properties
    if centered:
        pPr = ET.SubElement(p, f'{{{W_NS}}}pPr')
        jc = ET.SubElement(pPr, f'{{{W_NS}}}jc')
        jc.set(f'{{{W_NS}}}val', 'center')
    
    # Run
    r = ET.SubElement(p, f'{{{W_NS}}}r')
    
    # Run properties
    if bold or font_size or font_name:
        rPr = ET.SubElement(r, f'{{{W_NS}}}rPr')
        if bold:
            b = ET.SubElement(rPr, f'{{{W_NS}}}b')
        if font_name:
            rFonts = ET.SubElement(rPr, f'{{{W_NS}}}rFonts')
            rFonts.set(f'{{{W_NS}}}ascii', font_name)
            rFonts.set(f'{{{W_NS}}}hAnsi', font_name)
            rFonts.set(f'{{{W_NS}}}cs', font_name)
        if font_size:
            sz = ET.SubElement(rPr, f'{{{W_NS}}}sz')
            sz.set(f'{{{W_NS}}}val', str(font_size))
            szCs = ET.SubElement(rPr, f'{{{W_NS}}}szCs')
            szCs.set(f'{{{W_NS}}}val', str(font_size))
    
    # Text
    t = ET.SubElement(r, f'{{{W_NS}}}t')
    t.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    t.text = text
    
    return p


def make_code_paragraph(text, font_name='Courier New', font_size=20):
    """Create a paragraph for code content (monospace font, size 10pt = 20 half-points)."""
    p = ET.SubElement(ET.Element('dummy'), f'{{{W_NS}}}p')
    
    # Paragraph properties - set spacing to 0
    pPr = ET.SubElement(p, f'{{{W_NS}}}pPr')
    spacing = ET.SubElement(pPr, f'{{{W_NS}}}spacing')
    spacing.set(f'{{{W_NS}}}after', '0')
    spacing.set(f'{{{W_NS}}}line', '240')
    spacing.set(f'{{{W_NS}}}lineRule', 'auto')
    
    # Run
    r = ET.SubElement(p, f'{{{W_NS}}}r')
    
    # Run properties
    rPr = ET.SubElement(r, f'{{{W_NS}}}rPr')
    rFonts = ET.SubElement(rPr, f'{{{W_NS}}}rFonts')
    rFonts.set(f'{{{W_NS}}}ascii', font_name)
    rFonts.set(f'{{{W_NS}}}hAnsi', font_name)
    rFonts.set(f'{{{W_NS}}}cs', font_name)
    sz = ET.SubElement(rPr, f'{{{W_NS}}}sz')
    sz.set(f'{{{W_NS}}}val', str(font_size))
    szCs = ET.SubElement(rPr, f'{{{W_NS}}}szCs')
    szCs.set(f'{{{W_NS}}}val', str(font_size))
    
    # Text
    t = ET.SubElement(r, f'{{{W_NS}}}t')
    t.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    t.text = text
    
    return p


def make_empty_paragraph():
    """Create an empty paragraph (line break)."""
    p = ET.SubElement(ET.Element('dummy'), f'{{{W_NS}}}p')
    return p


def make_page_break():
    """Create a paragraph with a page break."""
    p = ET.SubElement(ET.Element('dummy'), f'{{{W_NS}}}p')
    r = ET.SubElement(p, f'{{{W_NS}}}r')
    br = ET.SubElement(r, f'{{{W_NS}}}br')
    br.set(f'{{{W_NS}}}type', 'page')
    return p


def build_appendix_paragraphs(title, subtitle, classes_with_code):
    """
    Build a list of paragraph elements for an appendix.
    
    Args:
        title: e.g., "Приложение Б"
        subtitle: e.g., "Фрагмент листинга моделей данных приложения «EatTogether»"
        classes_with_code: list of tuples (class_name, code_content)
    
    Returns:
        list of ET.Element paragraphs
    """
    paragraphs = []
    
    # Page break before appendix
    paragraphs.append(make_page_break())
    
    # Title (centered, bold)
    paragraphs.append(make_paragraph(title, bold=True, font_size=28, centered=True))
    
    # "(обязательное)" (centered)
    paragraphs.append(make_paragraph('(обязательное)', font_size=28, centered=True))
    
    # Subtitle (centered)
    paragraphs.append(make_paragraph(subtitle, font_size=28, centered=True))
    
    # Empty line
    paragraphs.append(make_empty_paragraph())
    
    for class_name, code_content in classes_with_code:
        # Class name header (bold)
        paragraphs.append(make_paragraph(class_name, bold=True, font_size=24))
        
        # Empty line before code
        paragraphs.append(make_empty_paragraph())
        
        # Code lines
        lines = code_content.split('\n')
        for line in lines:
            paragraphs.append(make_code_paragraph(line))
        
        # Empty line after code block
        paragraphs.append(make_empty_paragraph())
    
    return paragraphs


def main():
    # Read source files
    dish_code = read_source_file(DISH_CS)
    plate_code = read_source_file(PLATE_CS)
    family_code = read_source_file(FAMILY_CS)
    menu_vm_code = read_source_file(MENU_VM_CS)
    plate_vm_code = read_source_file(PLATE_VM_CS)
    
    # Register all known namespaces to preserve them
    for prefix, uri in NAMESPACES.items():
        ET.register_namespace(prefix, uri)
    
    # Create temp directory for extraction
    temp_dir = tempfile.mkdtemp()
    
    try:
        # Extract docx
        with zipfile.ZipFile(DOCX_PATH, 'r') as zin:
            zin.extractall(temp_dir)
            file_list = zin.namelist()
        
        # Parse document.xml
        doc_xml_path = os.path.join(temp_dir, 'word', 'document.xml')
        tree = ET.parse(doc_xml_path)
        root = tree.getroot()
        
        # Find the body element
        body = root.find(f'{{{W_NS}}}body')
        if body is None:
            print("ERROR: Could not find w:body element!")
            return
        
        # The body might have a sectPr element at the end that defines page layout
        # We need to insert our paragraphs BEFORE that sectPr
        sect_pr = body.find(f'{{{W_NS}}}sectPr')
        
        # Build paragraphs for Приложение Б
        appendix_b = build_appendix_paragraphs(
            'Приложение Б',
            'Фрагмент листинга моделей данных приложения «EatTogether»',
            [
                ('Класс Dish.cs', dish_code),
                ('Класс Plate.cs', plate_code),
                ('Класс Family.cs', family_code),
            ]
        )
        
        # Build paragraphs for Приложение В
        appendix_v = build_appendix_paragraphs(
            'Приложение В',
            'Фрагмент листинга слоя представления приложения «EatTogether»',
            [
                ('Класс MenuViewModel.cs', menu_vm_code),
                ('Класс PlateViewModel.cs', plate_vm_code),
            ]
        )
        
        # Insert paragraphs before sectPr (or at end if no sectPr)
        all_new_paragraphs = appendix_b + appendix_v
        
        if sect_pr is not None:
            # Find the index of sectPr in body
            sect_pr_index = list(body).index(sect_pr)
            # Insert all paragraphs before sectPr
            for i, para in enumerate(all_new_paragraphs):
                body.insert(sect_pr_index + i, para)
        else:
            # Just append at the end
            for para in all_new_paragraphs:
                body.append(para)
        
        # Write modified XML back
        tree.write(doc_xml_path, xml_declaration=True, encoding='UTF-8')
        
        # Repack the docx
        # Create a new zip file
        output_path = DOCX_PATH + '.new'
        with zipfile.ZipFile(output_path, 'w', zipfile.ZIP_DEFLATED) as zout:
            for file_name in file_list:
                file_path = os.path.join(temp_dir, file_name)
                if os.path.isfile(file_path):
                    zout.write(file_path, file_name)
        
        # Replace original with new file
        os.replace(output_path, DOCX_PATH)
        print("SUCCESS: Document updated with Приложение Б and Приложение В")
        
    finally:
        # Clean up temp directory
        shutil.rmtree(temp_dir, ignore_errors=True)


if __name__ == '__main__':
    main()
