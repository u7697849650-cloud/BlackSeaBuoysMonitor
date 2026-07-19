package bg.blacksea.buoys.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import bg.blacksea.buoys.domain.DataSourceInfo
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun DataSourceCaption(source:DataSourceInfo,modifier:Modifier=Modifier,compact:Boolean=true){val instant=source.observationTime?:source.modelRunTime?:source.fetchedAt;val formatted=instant.atZone(ZoneId.of("Europe/Sofia")).format(DateTimeFormatter.ofPattern(if(compact)"HH:mm" else "dd.MM.yyyy, HH:mm"));val first=if(source.isForecast)tr("Прогноза","Forecast") else tr("Източник","Source");Column(modifier.semantics{contentDescription="$first: ${source.displayName}, $formatted"}){Text("$first: ${source.displayName}",style=MaterialTheme.typography.labelSmall);Text("${if(source.isForecast)tr("Обновено","Updated") else tr("Измерено","Measured")}: $formatted",style=MaterialTheme.typography.labelSmall)}}
