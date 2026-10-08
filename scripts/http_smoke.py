"""Pruebas HTTP contra despliegue conectado EXCLUSIVAMENTE a base de demostración.
Requiere DPAO_HTTP_TEST=true, DPAO_APP_USER y DPAO_APP_PASSWORD.
"""
import os, re, time, urllib.parse, urllib.request, http.cookiejar
from urllib.error import HTTPError
assert os.environ.get('DPAO_HTTP_TEST') == 'true', 'Confirme despliegue aislado con DPAO_HTTP_TEST=true'
BASE='http://localhost:8080/dpao-cookies-web'
class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args): return None
opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()),NoRedirect)
def request(path, data=None):
    raw=None if data is None else urllib.parse.urlencode(data).encode('utf-8')
    start=time.perf_counter()
    try: r=opener.open(BASE+path,raw)
    except HTTPError as e: r=e
    text=r.read().decode('utf-8')
    print(f'{"GET" if data is None else "POST"} {path}: HTTP {r.code}, {(time.perf_counter()-start)*1000:.1f} ms')
    return r.code, text, r.headers
def get(path):
    code,text,_=request(path); assert code==200,(path,code); return text
def post(path, data, page):
    data['csrf']=re.search(r'name="csrf" value="([^"]+)"',page).group(1)
    code,_,headers=request(path,data); assert code==302,(path,code)
    assert headers['Location'].endswith(path) or path=='/acceso' or path=='/salir'
code,_,h=request('/acceso')
assert code==200 and h.get('X-DPAO-Test-Database')=='isolated', 'El servidor no está conectado a dpao_test. No se modificarán datos.'
code,_,h=request('/insumos'); assert code==302 and h['Location'].endswith('/acceso')
page=get('/acceso')
post('/acceso',{'usuario':'incorrecto','clave':'incorrecta'},page)
page=get('/acceso'); assert 'Credenciales inválidas' in page
post('/acceso',{'usuario':os.environ['DPAO_APP_USER'],'clave':os.environ['DPAO_APP_PASSWORD']},page)
for path in ['/tablero','/insumos','/productos','/kardex']:
    page=get(path)
    assert 'Cerrar sesión' in page
    for link in ['/tablero','/insumos','/productos','/kardex']: assert '/dpao-cookies-web'+link in page
    assert 'Exception' not in page and 'No se pudo consultar' not in page
suffix=str(int(time.time()))
page=get('/insumos')
data=dict(accion='registrar',codigo='H'+suffix,nombre='Azúcar ñ HTTP',unidadMedida='kg',stockActual='5.00',stockMinimo='5.00',costoUnitario='0.10',idCategoria='1')
post('/insumos',data.copy(),page)
page=get('/insumos'); assert 'Insumo registrado correctamente' in page and 'Azúcar ñ HTTP' in page
assert '[REABASTECER]' in page
post('/insumos',data.copy(),page); page=get('/insumos'); assert 'El código ya existe' in page
for field,value in [('codigo','X'*21),('nombre',''),('stockActual','-1'),('stockActual','0.001'),('stockActual','100000000'),('idCategoria','999999')]:
    invalid=data.copy(); invalid['codigo']='E'+suffix; invalid[field]=value
    post('/insumos',invalid,page); page=get('/insumos'); assert 'role="alert"' in page
assert 'role="alert"' not in get('/insumos'), 'PRG consume el mensaje una sola vez'
page=get('/productos')
post('/productos',dict(id='0',codigo='P'+suffix,nombre='Galleta ñ HTTP',precio='6.50',categoria='2'),page)
page=get('/productos'); assert 'Producto registrado correctamente' in page and 'Galleta ñ HTTP' in page
pid=re.search(r'<td>(\d+)</td>\s*<td class="code">P'+suffix, page).group(1)
post('/productos',dict(id=pid,codigo='P'+suffix,nombre='Galleta cacao HTTP',precio='7.50',categoria='2'),page)
page=get('/productos'); assert 'Producto actualizado correctamente' in page and 'Galleta cacao HTTP' in page
page=get('/kardex'); assert f'value="PRODUCTO:{pid}"' in page
post('/kardex',dict(item='PRODUCTO:'+pid,tipo='ENTRADA',cantidad='3.00',motivo='Horneado ñ HTTP'),page)
page=get('/kardex'); assert 'Horneado ñ HTTP' in page and 'stock 0.00 → 3.00' in page
post('/kardex',dict(item='PRODUCTO:'+pid,tipo='VENTA',cantidad='1.00',motivo='Venta HTTP'),page)
page=get('/kardex?tipo=VENTA'); assert 'Venta HTTP' in page and 'Horneado ñ HTTP' not in page
page=get('/kardex?desde=2099-01-01&hasta=2099-01-02'); assert 'Sin movimientos' in page
page=get('/kardex?desde=2099-01-02&hasta=2099-01-01'); assert 'La fecha inicial no puede ser posterior' in page
page=get('/kardex')
post('/kardex',dict(item='PRODUCTO:'+pid,tipo='SALIDA',cantidad='99.00',motivo='Insuficiente'),page)
page=get('/kardex'); assert 'Stock insuficiente: disponible 2.00, solicitado 99.00.' in page and 'Insuficiente</td>' not in page
post('/kardex',dict(item='PRODUCTO:'+pid,tipo='AJUSTE',cantidad='5.00',motivo='Conteo HTTP'),page)
page=get('/kardex'); assert 'stock 2.00 → 5.00' in page and 'Conteo HTTP' in page
post('/kardex',dict(item='PRODUCTO:'+pid,tipo='AJUSTE',cantidad='5.00',motivo='Conteo sin cambio'),page)
page=get('/kardex'); assert 'no se registró ningún movimiento' in page and 'Conteo sin cambio' not in page
page=get('/insumos')
fila=re.search(r'name="idInsumo" value="(\d+)">\s*<input[^>]*value="([\d.]+)"',page)
iid,nuevo=fila.group(1),f'{float(fila.group(2))+0.25:.2f}'
post('/insumos',dict(accion='actualizarStock',idInsumo=iid,nuevoStock=nuevo),page)
page=get('/insumos'); assert 'ajuste registrado en Kardex' in page
assert 'Últimos movimientos' in get('/tablero')
code,_,_=request('/productos',dict(id='0')); assert code==403,'CSRF'
code,_,_=request('/WEB-INF/views/productos.jsp'); assert code==404
post('/salir',{},page)
code,_,h=request('/tablero'); assert code==302 and h['Location'].endswith('/acceso')
print('PASS: navegación de cinco vistas, autenticación, PRG, UTF-8, registro, edición, validaciones, Kardex con saldos, stock insuficiente, ajustes, filtros, CSRF y cierre de sesión.')
