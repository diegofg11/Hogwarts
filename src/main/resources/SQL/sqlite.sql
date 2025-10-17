CREATE TABLE Estudiantes (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    Nombre TEXT,
    Apellidos TEXT,
    Curso TEXT,
    Patronus TEXT,
    Casa TEXT
);

-- Ejemplo de datos sincronizados
INSERT INTO estudiantes_global (Nombre, Apellidos, Curso, Patronus, Casa) VALUES
('Harry', 'Potter', '5º', 'Ciervo', 'Gryffindor'),
('Luna', 'Lovegood', '5º', 'Liebre', 'Ravenclaw');
